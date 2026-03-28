import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${process.env.NEXT_PUBLIC_API_ROOT || "http://localhost:8087"}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
