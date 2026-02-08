``` BASIC FLOW ```
What happens when a user connects Gmail?
Step-by-step real flow

1️⃣ User logs in via Keycloak
2️⃣ Gateway forwards JWT to AuthHub
3️⃣ AuthHub extracts principalId
4️⃣ User clicks “Connect Gmail”
5️⃣ OAuth callback returns tokens
6️⃣ AuthHub stores


--------------------------```PROJECT STRUCTURE USED ```--------------------------

ai.mailhub.authhub
├── adapter
│   ├── in
│   │   └── web
│   │        ├── GetValidAccessTokenController
│   │        ├── OAuthAuthorizationController
│   │        └── OAuthCallbackController
│   └── out
│         ├── oauth
│         │     ├── gmail
│         │     │      └── GmailOauthProviderClient
│         │     ├── OAuthProvider
│         │     ├── OAuthProviderRegistry
│         │     └── OAuthProvidersConfig
│         └── persistence
│                └── R2dbcOAuthAccountRepository.java
│
├── application
│    └── port
│    │      └── in
│    │      └── out
│    │           └── OAuthAccountRepository
│    │           └── OAuthProviderClient
│    ├── usecase
│    │   └── GetValidAccessTokenUseCase.java
│    └── service
│         ├── JwtPrincipalExtractor.java
│         └── PrincipalIdService
│
├── config
│   ├── CryptoConfig
│   └── TimeConfig
│
├── crypto
│   └── PrincipalIdUtil
│
│
├── domain
│   │   ├── identity
│   │   │    └── ExternalPrincipal
│   │   └── oauth
│   │        ├── OAuthAccount.java
│   │        ├── OAuthAccountStatus.java
│   │        ├── OAuthStateContext.java
│   │        ├── OAuthToken.java
│   │        └── OAuthTokens.java
│   └── exception
│       └── UserAlreadyExistsException(TODO)
│
├── infrastructure
│   ├── oauth
│   │     ├── registry
│   │     │        └── OAuthProviderClientRegistry.java
│   │     └── OAuthTokenRefresher.java
│   └── security
│       └── SecurityConfig.java
├── utils
│      └── OAuthStateUtil.java
├── config
│     └── CryptoConfig.java
│     └── TimeConfig.java
├── crypto
│     └── PrincipalIdUtil.java
│
└── security
usecase → what the system does
service → helpers used by many use cases
domain → what the system knows
infrastructure → how it talks to the world

Responsibility breakdown (important)

api
HTTP / REST / GraphQL
Request validation
Authentication context
Mapping DTO ↔ domain
NO business logic

application
Use cases
Transaction boundaries
Orchestration
Calls domain logic
Talks via interfaces

domain
Business rules
Entities & value objects
Domain exceptions
Repository interfaces
Framework-free

infrastructure
Database
Web clients
Messaging
Security
Framework implementations



--------------------------``` BASIC PROJECT SETUP INFO ```--------------------------

Anything HTTP/provider-specific → infrastructure
Anything business rules → application / domain

Folder structure

ai.mailhub.authhub
│
├── application/
│   └── GetValidAccessToken.java
│
├── domain(PURE domain (no Spring))
│   ├── identity
│   │       └── ExternalPrincipal(Represents “who the user is” independent of Keycloak)
│   └── oauth
│           ├──OAuthProvider (Configuration model, loaded via YAML / DB, for authorization + refresh flows )
│           ├──OAuthAccount (Central secure storage, Owns token lifecycle, owned by ExternalPrincipal )
│           ├──OAuthToken (Runtime-only value object)
│           └──OAuthAccountStatus (Account active, revoked etc )
│
├── crypto(Crypto utilities)
│        └──PrincipalIdUtil(Pure Crypto util, easy to replace later)
│
├── repository/ ← INTERFACES (ports)(Defines what the app needs, not how, used by Application layer)
│   └── OAuthAccountRepository.java
│
├── infrastructure
│   ├── repository( Adapter implementation for repository, Knows Postgres, R2DBC, SQL)
│   │         └── R2dbcOAuthAccountRepository.java
│   ├── redis
│   ├── oauth
│   ├── crypto
│   └── security
│
├── config
│   └── CryptoConfig
├── service/                    ← Spring services
│       └── PrincipalIdService.java
│
└── AuthHubApplication.java


Folder-by-Folder Explanation 

1)  api/ — Controllers (HTTP layer)
👉 This is where your Controllers live.
Equivalent to:

@RestController
@RequestMapping("/auth")
public class AuthController { ... }
What this layer does
•	Handles HTTP requests
•	Reads path/query/body
•	Converts HTTP → application call
•	Converts result → HTTP response
What it must NOT do
❌ OAuth logic ❌ Token refresh ❌ Encryption ❌ Database access
Example

api/
├─ ExternalOAuthController.java
├─ TokenController.java
└─ AuthorizationController.java
These are thin controllers by design.

2) application/ — Services (Use Cases)
👉 This IS your service layer, just done correctly.
Instead of one massive AuthService, you get:
•	One class per business capability
•	Clear intent
•	Easier testing
Example

application/
├─ HandleOAuthCallbackUseCase.java
├─ GetValidAccessTokenUseCase.java
├─ RevokeProviderAccessUseCase.java
└─ AuthorizeActionUseCase.java
Why this is better than AuthService
Traditional:

AuthService.doEverything()
AuthHub-style:

GetValidAccessTokenUseCase.execute()
Each use case:
•	Has one responsibility
•	Is easy to reason about
•	Is easy to secure

3)  domain/ — Core Logic (No Spring, No HTTP)
👉 This is the heart of AuthHub.
If you delete Spring, WebFlux, Redis — this should still compile.

4)  domain/model/
Pure business objects.
Example:

ExternalOAuthAccount
ExternalOAuthToken
EncryptedValue
AccountStatus
These:
•	Represent reality
•	Have no annotations
•	Are immutable where possible

5) domain/repository/
Interfaces only.
Example:

interface ExternalOAuthTokenRepository {
Mono<ExternalOAuthToken> findActiveByUserAndProvider(...);
}
Why?
•	Domain says what it needs
•	Infrastructure decides how

6) domain/crypto/
Security abstraction.
Example:

public interface CryptoService {
EncryptedValue encrypt(String plaintext);
String decrypt(EncryptedValue encrypted);
}
Domain requires encryption, but does not care:
•	Vault
•	KMS
•	Local AES

7) domain/policy/
Authorization logic (PDP).
Example:

PolicyEvaluator
PolicyDecision
Permission
Used by /auth/authorize.

8) infrastructure/ — Dirty Details (On Purpose)
👉 Everything here is replaceable plumbing.

a) infrastructure/persistence/
•	R2DBC entities
•	Spring repositories
•	Mapping DB ↔ domain
This is where SQL lives.

b) infrastructure/redis/
•	OAuth state storage
•	Access-token cache
•	Refresh locks
•	Blacklists
Redis is performance + coordination, not truth.

c) infrastructure/oauth/
Provider-specific logic.

GoogleOAuthClient
MicrosoftOAuthClient
These know:
•	Token endpoint URLs
•	Parameter quirks
•	Error formats

d) infrastructure/crypto/
Concrete encryption implementations.

VaultTransitCryptoService
LocalAesCryptoService
Both implement CryptoService.

e) infrastructure/security/
•	Spring Security config
•	JWT decoders
•	(Later) AuthHub token issuer

9) config/ — Wiring Only
•	Bean configuration
•	Conditional beans (dev/prod)
•	Provider registry config



Spring reactive web
H2Database
Spring R2DBC -> Provides configuration and dependencies for R2DBC
Does not include any specific DB driver
Com.h2database is H2 DB dependency
And io.r2dbc is the h2 driver, provides reactive interface to interact with H2 DB using R2DBC. For MySql then driver for MySql Will need to be added

Actuator -> Links for health of the application
Devtools -> Provide liver reload , restarts -> only for local development 