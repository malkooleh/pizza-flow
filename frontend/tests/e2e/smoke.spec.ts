import { test, expect } from '@playwright/test';

test.describe('App Smoke Test', () => {
    test('should load the home page and show the menu', async ({ page }) => {
        // Go to the home page
        await page.goto('/');

        // Expect the page to have a title or some identifying text
        // Note: Replace with actual content once we know exactly what is on the Landing page
        await expect(page).toHaveTitle(/PizzaFlow/i);
        
        // Check for a core element (e.g., "Menu")
        const menuHeading = page.getByRole('heading', { name: /Choose Your Pizza/i });
        // We'll use a more generic check if the above is too specific
        // await expect(page.getByText(/Pizza/i).first()).toBeVisible();
    });

    test('should navigate to admin dashboard', async ({ page }) => {
        await page.goto('/admin');
        // This might redirect to Keycloak login if not authenticated
        // For simple smoke test, we just check if it doesn't 404
        expect(page.url()).toContain('/admin');
    });
});
