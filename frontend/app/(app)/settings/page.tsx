"use client";

import { useAuth } from "@/hooks/use-auth";
import { Separator } from "@/components/ui/separator";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export default function SettingsPage() {
  const { user } = useAuth();

  return (
    <div className="flex flex-1 flex-col gap-6 p-6">
      <div>
        <h1 className="text-xl font-semibold">Settings</h1>
        <p className="text-muted-foreground mt-1 text-sm">Account settings and preferences.</p>
      </div>
      <Separator />
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Profile</CardTitle>
            <CardDescription>Your account information</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <span className="text-muted-foreground text-sm">Name</span>
              <p className="font-medium">{user?.fullName}</p>
            </div>
            <div>
              <span className="text-muted-foreground text-sm">Email</span>
              <p className="font-medium">{user?.email}</p>
            </div>
            <div>
              <span className="text-muted-foreground text-sm">Role</span>
              <p className="font-medium">{user?.staff ? "Staff" : "Member"}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Groups & Permissions</CardTitle>
            <CardDescription>Your access level</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <span className="text-muted-foreground text-sm">Groups</span>
              <p className="font-medium">{user?.groups?.join(", ") || "None"}</p>
            </div>
            <div>
              <span className="text-muted-foreground text-sm">Permissions</span>
              <div className="mt-1 flex flex-wrap gap-1">
                {user?.permissions?.map((p) => (
                  <span key={p} className="bg-secondary rounded px-2 py-0.5 text-xs">
                    {p}
                  </span>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
