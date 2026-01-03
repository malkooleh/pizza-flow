# Keycloak Setup Guide

Since we are running a fresh Keycloak instance in Docker, we need to configure the Realm, Client, and Users manually (or import a config).

## 1. Login to Admin Console
- **URL**: [http://localhost:8081](http://localhost:8081)
- **Username**: `admin`
- **Password**: `admin`

## 2. Create Realm
1.  Hover over "Master" in the top-left corner and click **Create Realm**.
2.  **Realm name**: `pizza-flow`
3.  Click **Create**.

## 3. Create Client
1.  Go to **Clients** > **Create client**.
2.  **Client type**: `OpenID Connect`.
3.  **Client ID**: `pizza-flow-web`
4.  Click **Next**.
5.  **Client authentication**: `Off` (Public client - we will use "password" grant for testing).
6.  **Authentication flow**:
    - [x] Standard flow
    - [x] Direct access grants (Important for our .rest testing)
7.  Click **Save**.

## 4. Create User
1.  Go to **Users** > **Add user**.
2.  **Username**: `pizza-user`
3.  **First name**: `Pizza`
4.  **Last name**: `Lover`
5.  Click **Create**.
6.  Go to the **Credentials** tab.
7.  **Set password**.
8.  **Password**: `password`
9.  **Temporary**: `Off` (Perform toggle off).
10. Click **Save**.

## 5. Get Token
You can now use the `docs/api/api-gateway.rest` file to fetch a token using the username `pizza-user` and password `password`.
