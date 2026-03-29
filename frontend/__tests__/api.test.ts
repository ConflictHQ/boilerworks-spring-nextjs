import { describe, it, expect, vi, beforeEach } from "vitest";

describe("API module", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("apiGet sends GET request with credentials", async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve({ ok: true, data: [1, 2, 3], errors: null }),
    });
    global.fetch = mockFetch;

    const { apiGet } = await import("@/lib/api");
    const result = await apiGet("/api/items");

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/items"),
      expect.objectContaining({ credentials: "include" })
    );
    expect(result.ok).toBe(true);
    expect(result.data).toEqual([1, 2, 3]);
  });

  it("apiPost sends POST request with body", async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve({ ok: true, data: { id: "1" }, errors: null }),
    });
    global.fetch = mockFetch;

    const { apiPost } = await import("@/lib/api");
    const result = await apiPost("/api/items", { name: "Test" });

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/items"),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ name: "Test" }),
      })
    );
    expect(result.ok).toBe(true);
  });
});
