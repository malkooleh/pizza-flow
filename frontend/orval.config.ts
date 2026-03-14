import { defineConfig } from 'orval';

export default defineConfig({
    orderService: {
        input: 'http://localhost:8082/v3/api-docs',
        output: {
            mode: 'tags-split',
            target: 'src/api/orderService/orderService.ts',
            schemas: 'src/api/orderService/model',
            client: 'react-query',
            httpClient: 'axios',
        },
    },
    catalogService: {
        input: 'http://localhost:8083/v3/api-docs',
        output: {
            mode: 'tags-split',
            target: 'src/api/catalogService/catalogService.ts',
            schemas: 'src/api/catalogService/model',
            client: 'react-query',
            httpClient: 'axios',
        },
    },
    inventoryService: {
        input: 'http://localhost:8087/v3/api-docs',
        output: {
            mode: 'tags-split',
            target: 'src/api/inventoryService/inventoryService.ts',
            schemas: 'src/api/inventoryService/model',
            client: 'react-query',
            httpClient: 'axios',
        },
    },
    bookingService: {
        input: 'http://localhost:8086/v3/api-docs',
        output: {
            mode: 'tags-split',
            target: 'src/api/bookingService/bookingService.ts',
            schemas: 'src/api/bookingService/model',
            client: 'react-query',
            httpClient: 'axios',
        },
    },
});
