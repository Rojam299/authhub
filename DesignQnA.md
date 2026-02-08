```BASIC FLOW ```
What happens when a user connects Gmail?
Step-by-step real flow

1️⃣ User logs in via Keycloak
2️⃣ Gateway forwards JWT to AuthHub
3️⃣ AuthHub extracts principalId
4️⃣ User clicks “Connect Gmail”
5️⃣ OAuth callback returns tokens
6️⃣ AuthHub stores:

--------------------------``` HEXAGONAL PROJECT ```--------------------------

EXAMPLE
src/main/java
└── com.example.orders
├── domain( Business logic, Framework independent )
│   ├── model
│   │   └── Order.java
│   ├── service
│   │   └── OrderService.java
│   └── exception
│       └── OrderNotFoundException.java
│
├── application - Use Cases & Ports
│   ├── port
│   │   ├── in (Defines what the application can do. Called by controllers, messaging, etc. )
│   │   │   └── PlaceOrderUseCase.java
│   │   └── out (Defines what the application needs from outside. Implemented by DB, REST)
│   │       └── OrderRepository.java
│   └── usecase (Implements inbound ports and orchestrates business logic.)
│         └── PlaceOrderService.java
│
├── adapter (External World Integration)
│   ├── in (How requests enter the application.)
│   │   └── web
│   │       └── OrderController.java
│   └── out (How the app talks to external systems.)
│       ├── persistence
│       │   └── JpaOrderRepository.java
│       └── messaging
│           └── OrderEventPublisher.java
│
└── config
└── BeanConfiguration.java

## Hexagonal Architecture structures application so that:
Business logic is isolated
External systems (DB, UI, APIs, messaging, frameworks) are replaceable
The core of does not depend on frameworks.
Dependencies always point inward (toward business logic).

__**Core Concepts** ->
## Domain (Core - business rules, framework independent)
Pure business rules
No framework, no database, no HTTP
Plain Java objects (POJOs)

## Ports
Interfaces that define what the application needs or what it offers.
Inbound Ports → How the outside world calls your app. How requests enter the application
Outbound Ports → What the app needs from outside systems

## Adapters
Implement the ports.
Inbound Adapters → REST controllers, CLI, messaging consumers. How requests enter the application.
Outbound Adapters → Database repositories, REST clients, message producers.
How the app talks to external systems.

```Application – Use Cases & Ports```
Inbound Ports (port/in)  - Defines what the application can do(interface).
Outbound Ports (port/out) - Defines what the application needs from outside(ex - interface OrderRepository).
Use Case Implementation (usecase) - Implements inbound ports and orchestrates business logic

## Dependency Direction
Adapters → Application → Domain

| Benefit                | Explanation                                      |
|------------------------|--------------------------------------------------|
| Testability            | Core logic tested without DB, web, or Spring     |
| Flexibility            | Swap DB, API, UI without touching business logic |
| Maintainability        | Clear boundaries and responsibilities            |
| Framework independence | Spring is a detail, not the foundation           |
| Long-term scalability  | Ideal for evolving systems                       |


```=======================================QAndA ==================```

# Q) Who puts JWT into the SecurityContext? -> Answer: Spring Security filter chain

When we add : 
## spring-boot-starter-oauth2-resource-server 
and use code
## http.oauth2ResourceServer(oauth -> oauth.jwt());
spring registers -> BearerTokenAuthenticationWebFilter
and this filter places JWT into security context(which in turn is placed in reactor context)

Reactor Context is ATTACHED to the reactive subscription created for each HTTP request.
Each HTTP request creates its own reactive subscription, and the Reactor Context 
is attached to that subscription and EXISTS ONLY FOR the LIFETIME of that reactive chain.

# Per request/pipeline:
A new Reactor Context is created
A new SecurityContext is attached to it

## you should never parse header yourself in application code.
## NEVER DO
```request.getHeaders().get("Authorization")```

Why?
You bypass security filters
You re-implement validation
You risk accepting invalid tokens
You break testability

Header → Security Filter → JWT → SecurityContext → Reactor Context → Your Code
```You don’t pull JWT from headers. Spring pushes JWT into context.```

## For each incoming HTTP request:
Netty receives the request
Spring WebFlux creates a new reactive pipeline
That pipeline is subscribed
A fresh Reactor Context is created for that subscription
Spring Security puts a SecurityContext into that Reactor Context

# Flow :
HTTP request arrives
↓
Authorization: Bearer <JWT>
↓
Spring Security filter:
- Extracts token from header
- Validates signature (JWKS)
- Validates issuer, expiry
- Builds Jwt object
  ↓
  Creates Authentication:
  JwtAuthenticationToken
  ↓
  Stores it in SecurityContext
  ↓
  SecurityContext is placed in Reactor Context
  ↓
  Controller / service runs

Each HTTP request carries its own JWT in the Authorization header.

Single final JwtPrincipalExtractor class
Multiple Reactor Contexts
Multiple SecurityContexts
Each context is per request / per subscriber (user) 

Request A → Subscription A → Reactor Context A → SecurityContext A → JWT A
Request B → Subscription B → Reactor Context B → SecurityContext B → JWT B
Request C → Subscription C → Reactor Context C → SecurityContext C → JWT C



# Q) Where does principalId come from

HTTP Request
↓
Authorization: Bearer <Keycloak JWT>
↓
Spring Security validates JWT
↓
We read:
- iss (issuer)
- sub (subject)
  ↓
  ExternalPrincipal(issuer, subject)
  ↓
  principalId = derive(issuer + sub)


# Q) One service can be both OAuth Client and Resource Server.

AuthHub is:
Resource Server → for Keycloak tokens
OAuth Client → for Gmail / Outlook

Resource Server = validates incoming tokens
OAuth Client = obtains tokens from providers
AuthHub plays both roles, but at different times


# Q) Why we are NOT starting with Controller → Service → Repo

1) ```AuthHub is security infrastructure, not a CRUD app```
   If we start there, we end up designing around HTTP instead of security rules

2) ```Tokens & crypto must be correct before HTTP```
   Security systems are built inside-out, not outside-in.
   Bad order leads to:
      token leaks 
      refresh races
      unsafe logging
      untestable logic

3) ```Avoids the “god service” anti-pattern```

Typical flow
Controller → Service → Repo
           ↘ OAuth ↘ Redis ↘ Crypto

We build
Use case → domain → infra
Controller = thin wrapper


# Q) Why both Gateway and authub endpoints are secured using KC

## Gateway
Validates Keycloak user access token
Handles external traffic only

## Authub 
Two types of callers → two security modes

| User-context endpoints                  | Internal service endpoints                  |
|-----------------------------------------|---------------------------------------------|
| (e.g. link Gmail, get token from gmail) | (e.g. token fetch by Gmail MS)              |
|                                         |                                             |
| Secured with Keycloak user JWT          | Secured with service identity               |
| Called via Gateway                      | Either:                                     |
| Uses sub as userId                      | Keycloak client credentials, or             |
|                                         | AuthHub-issued internal JWT                 |
| Examples:                               |                                             |
| POST /oauth/{provider}/authorize        | Examples:                                   |
| GET /tokens/{provider}                  | GET /internal/tokens/{provider}             |
| AuthHub = resource server               |                                             |















