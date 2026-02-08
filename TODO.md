1) Redis lock around token refresh
2)  Provide scope selection in UI like only read mail which define boundary of actions using scope
    like can only read mail or can read and draft mail, can delete, full control and pass these scopes
    downstream till AuthHub. Authub will authenticate and fetch token accordingly. 
    Reauthenticate if user changes the scope 