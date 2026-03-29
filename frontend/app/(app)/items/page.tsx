"use client";

import { useCallback, useEffect, useState } from "react";
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api";
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
import { Plus, Pencil, Trash2 } from "lucide-react";
import { toast } from "sonner";

interface Item {
  id: string;
  name: string;
  slug: string;
  description: string;
  price: number;
  sku: string;
  active: boolean;
  categoryId: string | null;
  categoryName: string | null;
  createdAt: string;
}

export default function ItemsPage() {
  const { hasPermission } = useAuth();
  const [items, setItems] = useState<Item[]>([]);
  const [search, setSearch] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingItem, setEditingItem] = useState<Item | null>(null);
  const [form, setForm] = useState({ name: "", slug: "", description: "", price: "", sku: "", active: true });

  const loadItems = useCallback(async () => {
    const url = search ? `/api/items?search=${encodeURIComponent(search)}` : "/api/items";
    const res = await apiGet<Item[]>(url);
    if (res.ok && res.data) setItems(res.data);
  }, [search]);

  useEffect(() => {
    loadItems();
  }, [loadItems]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = { ...form, price: parseFloat(form.price) };

    const res = editingItem
      ? await apiPut<Item>(`/api/items/${editingItem.id}`, payload)
      : await apiPost<Item>("/api/items", payload);

    if (res.ok) {
      toast.success(editingItem ? "Item updated" : "Item created");
      setShowForm(false);
      setEditingItem(null);
      setForm({ name: "", slug: "", description: "", price: "", sku: "", active: true });
      loadItems();
    } else {
      toast.error(res.errors?.[0]?.messages?.[0] || "Failed to save item");
    }
  };

  const handleEdit = (item: Item) => {
    setEditingItem(item);
    setForm({
      name: item.name,
      slug: item.slug,
      description: item.description || "",
      price: String(item.price),
      sku: item.sku,
      active: item.active,
    });
    setShowForm(true);
  };

  const handleDelete = async (id: string) => {
    const res = await apiDelete(`/api/items/${id}`);
    if (res.ok) {
      toast.success("Item deleted");
      loadItems();
    }
  };

  return (
    <div className="flex flex-1 flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">Items</h1>
          <p className="mt-1 text-sm text-muted-foreground">Manage your item catalogue.</p>
        </div>
        {hasPermission("items.create") && (
          <Button onClick={() => { setShowForm(!showForm); setEditingItem(null); setForm({ name: "", slug: "", description: "", price: "", sku: "", active: true }); }}>
            <Plus className="mr-2 h-4 w-4" />
            Add Item
          </Button>
        )}
      </div>
      <Separator />

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle>{editingItem ? "Edit Item" : "New Item"}</CardTitle>
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
                <Label>Price</Label>
                <Input type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} required />
              </div>
              <div className="space-y-2">
                <Label>SKU</Label>
                <Input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} required />
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label>Description</Label>
                <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
              </div>
              <div className="flex gap-2 md:col-span-2">
                <Button type="submit">{editingItem ? "Update" : "Create"}</Button>
                <Button type="button" variant="outline" onClick={() => { setShowForm(false); setEditingItem(null); }}>Cancel</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <div className="flex items-center gap-4">
        <Input
          placeholder="Search items..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-sm"
        />
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>SKU</TableHead>
            <TableHead>Price</TableHead>
            <TableHead>Category</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="w-24">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {items.map((item) => (
            <TableRow key={item.id}>
              <TableCell className="font-medium">{item.name}</TableCell>
              <TableCell>{item.sku}</TableCell>
              <TableCell>${item.price.toFixed(2)}</TableCell>
              <TableCell>{item.categoryName || "--"}</TableCell>
              <TableCell>
                <span className={item.active ? "text-green-500" : "text-muted-foreground"}>
                  {item.active ? "Active" : "Inactive"}
                </span>
              </TableCell>
              <TableCell>
                <div className="flex gap-1">
                  {hasPermission("items.edit") && (
                    <Button variant="ghost" size="icon" onClick={() => handleEdit(item)}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                  )}
                  {hasPermission("items.delete") && (
                    <Button variant="ghost" size="icon" onClick={() => handleDelete(item.id)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              </TableCell>
            </TableRow>
          ))}
          {items.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} className="text-center text-muted-foreground">
                No items found.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
}
