package com.increff.ironic.pos.model.data;

import com.increff.ironic.pos.pojo.OrderItem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderItemChanges {

    private final List<OrderItem> itemsToUpdate, itemsToDelete, itemsToAdd;
    private final List<OrderItem> oldOrderItems, newOrderItems;

    public OrderItemChanges(List<OrderItem> oldOrderItems, List<OrderItem> newOrderItems) {
        itemsToUpdate = new LinkedList<>();
        itemsToDelete = new LinkedList<>();
        itemsToAdd = new LinkedList<>();
        this.oldOrderItems = oldOrderItems;
        this.newOrderItems = newOrderItems;
        computeChanges();
    }

    public List<OrderItem> getItemsToUpdate() {
        return itemsToUpdate;
    }

    public List<OrderItem> getItemsToDelete() {
        return itemsToDelete;
    }

    public List<OrderItem> getItemsToAdd() {
        return itemsToAdd;
    }

    private void computeChanges() {
        Map<Integer, OrderItem> oldItemMap = getItemMap(this.oldOrderItems);

        for (OrderItem newItem : this.newOrderItems) {
            int productId = newItem.getProductId();

            if (oldItemMap.containsKey(productId)) {
                int oldItemId = oldItemMap.get(productId).getId();
                newItem.setId(oldItemId);
                itemsToUpdate.add(newItem);
                oldItemMap.remove(productId);
            } else {
                itemsToAdd.add(newItem);
            }
        }

        itemsToDelete.addAll(oldItemMap.values());
    }

    private Map<Integer, OrderItem> getItemMap(List<OrderItem> items) {
        Map<Integer, OrderItem> itemMap = new HashMap<>();
        for (OrderItem item : items) {
            itemMap.put(item.getProductId(), item);
        }
        return itemMap;
    }

    private List<Integer> getQuantity(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getQuantity)
                .collect(Collectors.toList());
    }

    public List<Integer> getRequiredQuantities() {
        Map<Integer, OrderItem> oldItemMap = getItemMap(this.oldOrderItems);

        List<Integer> quantities = getQuantity(itemsToAdd);

        for (OrderItem newItem : itemsToUpdate) {
            OrderItem oldItem = oldItemMap.get(newItem.getProductId());
            int required = newItem.getQuantity() - oldItem.getQuantity();
            quantities.add(required);
        }

        List<Integer> deleteQuantities = getQuantity(itemsToDelete)
                .stream()
                .map(it -> -1 * it)
                .collect(Collectors.toList());

        quantities.addAll(deleteQuantities);
        return quantities;
    }

    public List<OrderItem> getAllChanges() {
        List<OrderItem> items = new LinkedList<>();
        items.addAll(itemsToAdd);
        items.addAll(itemsToUpdate);
        items.addAll(itemsToDelete);
        return items;
    }

}

