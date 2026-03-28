"use client";

import { usePathname } from "next/navigation";
import Link from "next/link";
import { routeLabels } from "@/lib/routes";
import { Separator } from "@/components/ui/separator";

export function PageHeader() {
  const pathname = usePathname();
  const segments = pathname.split("/").filter(Boolean);

  return (
    <header className="flex h-14 items-center border-b px-6">
      <nav className="flex items-center gap-2 text-sm">
        {segments.map((segment, index) => {
          const path = "/" + segments.slice(0, index + 1).join("/");
          const label =
            routeLabels[segment] ||
            segment.charAt(0).toUpperCase() + segment.slice(1).replace(/-/g, " ");
          const isLast = index === segments.length - 1;

          return (
            <span key={path} className="flex items-center gap-2">
              {index > 0 && <span className="text-muted-foreground">/</span>}
              {isLast ? (
                <span className="font-medium">{label}</span>
              ) : (
                <Link href={path} className="text-muted-foreground hover:text-foreground">
                  {label}
                </Link>
              )}
            </span>
          );
        })}
      </nav>
    </header>
  );
}
