import { describe, it, expect } from "vitest";
import { routeLabels } from "@/lib/routes";

describe("Route labels", () => {
  it("has label for dashboard", () => {
    expect(routeLabels["dashboard"]).toBe("Dashboard");
  });

  it("has label for items", () => {
    expect(routeLabels["items"]).toBe("Items");
  });

  it("has labels for all main routes", () => {
    const required = ["dashboard", "items", "categories", "forms", "workflows", "settings"];
    for (const route of required) {
      expect(routeLabels[route]).toBeDefined();
    }
  });
});
