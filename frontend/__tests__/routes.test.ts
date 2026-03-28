import { describe, it, expect } from "vitest";
import { routeLabels } from "@/lib/routes";

describe("Route labels", () => {
  it("has label for dashboard", () => {
    expect(routeLabels["dashboard"]).toBe("Dashboard");
  });

  it("has label for products", () => {
    expect(routeLabels["products"]).toBe("Products");
  });

  it("has labels for all main routes", () => {
    const required = ["dashboard", "products", "categories", "forms", "workflows", "settings"];
    for (const route of required) {
      expect(routeLabels[route]).toBeDefined();
    }
  });
});
