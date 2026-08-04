# SHOP

一个基于 Paper 1.21 的 Minecraft 全球玩家商店插件。通过完全可视化的箱子 GUI 菜单，让玩家轻松实现物品的上架、购买与管理，支持离线经济结算与 NBT 数据完美保存。

---

## 截图展示(有点多)
<img width="321" height="79" alt="Image" src="https://github.com/user-attachments/assets/7300e886-5aad-479d-ba12-9a38465e313c" />

<img width="387" height="69" alt="Image" src="https://github.com/user-attachments/assets/94daf4ec-38b9-4bb3-9e68-2632df78ec7c" />

<img width="370" height="76" alt="Image" src="https://github.com/user-attachments/assets/d5b43e6f-29a2-4985-9758-10f73116489f" />

<img width="376" height="160" alt="Image" src="https://github.com/user-attachments/assets/2eeebee7-4a25-4461-896e-ed52c56112cf" />

<img width="377" height="187" alt="Image" src="https://github.com/user-attachments/assets/bd46efa7-e0e7-43db-b038-7d44bbc0e8a8" />

<img width="378" height="165" alt="Image" src="https://github.com/user-attachments/assets/faba7daf-e56c-4547-8a72-c618ea927e34" />

<img width="477" height="160" alt="Image" src="https://github.com/user-attachments/assets/be3290ba-5211-43aa-bff0-e38d87628360" />

<img width="402" height="166" alt="Image" src="https://github.com/user-attachments/assets/5a167388-3e80-4723-8120-6e3a30a89ff3" />

<img width="384" height="165" alt="Image" src="https://github.com/user-attachments/assets/3dd98a30-d78b-47b8-9afc-8b433b417e43" />

---

## ✨ 主要功能

- **箱子 GUI 全交互**：拒绝繁琐指令，商品上架、数量/单价调整（红绿蓝玻璃板调控）、翻页、购买全由可视化界面完成。
- **完美 NBT 保护**：采用 Bukkit 原生 Base64 序列化，物品的自定义名称、附魔、Lore 等复杂数据 100% 无损保留。
- **离线经济支持**：完美挂钩 Vault API，即使卖家离线，商品售出后的金币也会自动打入其账户。
- **智能背包扣减**：上架时只需副手拿一个“样品”，系统会自动全背包扫描扣除同类物品，突破副手数量限制（单次最高上架 128 个）。
- **个人商品管理**：专属“我的商品”页面，支持随时一键下架商品，物品自动安全退回背包。
- **7 日交易记录**：内置自动维护的日志系统，可视化展示全服最近 7 天的买卖明细（买家、卖家、数量、总价、时间），过期记录自动清理。

---

## 📋 命令与权限

| 命令 | 说明 | 权限 |
| :--- | :--- | :--- |
| `/shop` | 打开全球商店主菜单 | 默认所有玩家可用 |

> 权限节点：插件默认向所有玩家开放使用，无需特殊权限设定。

---

## ⚙️ 数据文件

插件完全基于 JSON 存储，首次运行会在 `plugins/shop` 目录下自动生成 `shop.json` (商品列表) 和 `transactions.json` (交易流水)：

**`shop.json` 结构示例:**
```json
[
  {
    "id": "e4f8d234-abcd-1234-efgh-5678ijklmnop",
    "sellerUUID": "玩家的UUID",
    "sellerName": "EtherCat",
    "itemBase64": "rO0ABXcEAAAA... (Base64加密的物品信息)",
    "amount": 128,
    "pricePerUnit": 15.0
  }
]

```

---

## 🔧 依赖与兼容

* **服务端**：Paper 1.21.4（或兼容 1.21 分支的其他服务端）
* **Java**：21
* **前置依赖**：Vault 以及经济插件（如 EssentialsX）

---

## 📬 反馈与贡献

如有问题或建议，欢迎提交 [Issue](https://www.google.com/search?q=https://github.com/Ether-Cats/SHOP/issues) 或 Pull Request。
