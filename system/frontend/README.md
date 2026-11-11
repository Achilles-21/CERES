# CERES teaching frontend

Vue 3 / Vite / TypeScript SPA. See [system setup](../README.md) for configuration and service prerequisites.

```sh
pnpm install --frozen-lockfile
cp .env.example .env
pnpm dev
pnpm build
node scripts/test-slidegen-resources.mjs
```

API, media and WebSocket proxies are configured in `.env`. Production deployments must implement the same proxy routes. Only public settings belong in `VITE_*` variables.
