"use client";

import { useCallback, useEffect, useState } from "react";
import { apiDelete, apiGet, apiPost } from "@/lib/api";
import { useAuth } from "@/hooks/use-auth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

interface FormDefinition {
  id: string;
  name: string;
  slug: string;
  description: string;
  active: boolean;
  version: number;
  createdAt: string;
}

export default function FormsPage() {
  const { hasPermission } = useAuth();
  const [forms, setForms] = useState<FormDefinition[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: "", slug: "", description: "" });

  const loadForms = useCallback(async () => {
    const res = await apiGet<FormDefinition[]>("/api/forms");
    if (res.ok && res.data) setForms(res.data);
  }, []);

  useEffect(() => {
    loadForms();
  }, [loadForms]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const res = await apiPost<FormDefinition>("/api/forms", {
      ...form,
      schemaJson: { type: "object", properties: {} },
      active: true,
    });

    if (res.ok) {
      toast.success("Form created");
      setShowForm(false);
      setForm({ name: "", slug: "", description: "" });
      loadForms();
    } else {
      toast.error(res.errors?.[0]?.messages?.[0] || "Failed to create form");
    }
  };

  const handleDelete = async (id: string) => {
    const res = await apiDelete(`/api/forms/${id}`);
    if (res.ok) {
      toast.success("Form deleted");
      loadForms();
    }
  };

  return (
    <div className="flex flex-1 flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">Forms</h1>
          <p className="mt-1 text-sm text-muted-foreground">Manage dynamic form definitions.</p>
        </div>
        {hasPermission("forms.create") && (
          <Button onClick={() => setShowForm(!showForm)}>
            <Plus className="mr-2 h-4 w-4" />
            New Form
          </Button>
        )}
      </div>
      <Separator />

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle>New Form Definition</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label>Name</Label>
                <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
              </div>
              <div className="space-y-2">
                <Label>Slug</Label>
                <Input value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} required />
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label>Description</Label>
                <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
              </div>
              <div className="flex gap-2 md:col-span-2">
                <Button type="submit">Create</Button>
                <Button type="button" variant="outline" onClick={() => setShowForm(false)}>Cancel</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Slug</TableHead>
            <TableHead>Version</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="w-24">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {forms.map((f) => (
            <TableRow key={f.id}>
              <TableCell className="font-medium">{f.name}</TableCell>
              <TableCell>{f.slug}</TableCell>
              <TableCell>v{f.version}</TableCell>
              <TableCell>
                <span className={f.active ? "text-green-500" : "text-muted-foreground"}>
                  {f.active ? "Active" : "Inactive"}
                </span>
              </TableCell>
              <TableCell>
                {hasPermission("forms.delete") && (
                  <Button variant="ghost" size="icon" onClick={() => handleDelete(f.id)}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                )}
              </TableCell>
            </TableRow>
          ))}
          {forms.length === 0 && (
            <TableRow>
              <TableCell colSpan={5} className="text-center text-muted-foreground">
                No forms defined yet.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
}
