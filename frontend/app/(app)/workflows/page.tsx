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
import { Plus, Trash2, Play } from "lucide-react";
import { toast } from "sonner";

interface WorkflowDefinition {
  id: string;
  name: string;
  slug: string;
  description: string;
  initialState: string;
  active: boolean;
  version: number;
  createdAt: string;
}

export default function WorkflowsPage() {
  const { hasPermission } = useAuth();
  const [workflows, setWorkflows] = useState<WorkflowDefinition[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: "", slug: "", description: "", initialState: "draft" });

  const loadWorkflows = useCallback(async () => {
    const res = await apiGet<WorkflowDefinition[]>("/api/workflows");
    if (res.ok && res.data) setWorkflows(res.data);
  }, []);

  useEffect(() => {
    loadWorkflows();
  }, [loadWorkflows]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const res = await apiPost<WorkflowDefinition>("/api/workflows", {
      ...form,
      statesJson: [
        { name: "draft", label: "Draft" },
        { name: "review", label: "Under Review" },
        { name: "approved", label: "Approved", isFinal: true },
        { name: "rejected", label: "Rejected", isFinal: true },
      ],
      transitionsJson: [
        { name: "submit", from: "draft", to: "review" },
        { name: "approve", from: "review", to: "approved" },
        { name: "reject", from: "review", to: "rejected" },
      ],
      active: true,
    });

    if (res.ok) {
      toast.success("Workflow created");
      setShowForm(false);
      setForm({ name: "", slug: "", description: "", initialState: "draft" });
      loadWorkflows();
    } else {
      toast.error(res.errors?.[0]?.messages?.[0] || "Failed to create workflow");
    }
  };

  const handleStartInstance = async (id: string) => {
    const res = await apiPost(`/api/workflows/${id}/instances`, {});
    if (res.ok) {
      toast.success("Workflow instance started");
    } else {
      toast.error("Failed to start instance");
    }
  };

  const handleDelete = async (id: string) => {
    const res = await apiDelete(`/api/workflows/${id}`);
    if (res.ok) {
      toast.success("Workflow deleted");
      loadWorkflows();
    }
  };

  return (
    <div className="flex flex-1 flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">Workflows</h1>
          <p className="mt-1 text-sm text-muted-foreground">Define and manage state machine workflows.</p>
        </div>
        {hasPermission("workflows.create") && (
          <Button onClick={() => setShowForm(!showForm)}>
            <Plus className="mr-2 h-4 w-4" />
            New Workflow
          </Button>
        )}
      </div>
      <Separator />

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle>New Workflow Definition</CardTitle>
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
              <div className="space-y-2">
                <Label>Initial State</Label>
                <Input value={form.initialState} onChange={(e) => setForm({ ...form, initialState: e.target.value })} required />
              </div>
              <div className="space-y-2">
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
            <TableHead>Initial State</TableHead>
            <TableHead>Version</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="w-32">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {workflows.map((wf) => (
            <TableRow key={wf.id}>
              <TableCell className="font-medium">{wf.name}</TableCell>
              <TableCell>{wf.slug}</TableCell>
              <TableCell>{wf.initialState}</TableCell>
              <TableCell>v{wf.version}</TableCell>
              <TableCell>
                <span className={wf.active ? "text-green-500" : "text-muted-foreground"}>
                  {wf.active ? "Active" : "Inactive"}
                </span>
              </TableCell>
              <TableCell>
                <div className="flex gap-1">
                  {hasPermission("workflows.execute") && (
                    <Button variant="ghost" size="icon" onClick={() => handleStartInstance(wf.id)} title="Start instance">
                      <Play className="h-4 w-4" />
                    </Button>
                  )}
                  {hasPermission("workflows.delete") && (
                    <Button variant="ghost" size="icon" onClick={() => handleDelete(wf.id)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              </TableCell>
            </TableRow>
          ))}
          {workflows.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} className="text-center text-muted-foreground">
                No workflows defined yet.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
}
