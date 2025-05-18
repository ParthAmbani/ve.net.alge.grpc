"# ve.net.alge.grpc" 
Great — thanks for the extra detail. Here's the updated documentation with those clarifications included, noting that all paths are relative to the `ve.net.alge.grpc` plugin.

---

## 📘 **Document: Configuring gRPC in Postman**

---

### 🛠️ Prerequisites

- ✅ Postman version 10.14.0 or above  
- ✅ A running gRPC server (e.g., `localhost:50051`)  
- ✅ All relevant `.proto` files and dependencies stored locally  
- ✅ The plugin: `ve.net.alge.grpc` containing the source files

---

### 📂 Step 1: Create a New gRPC Request

1. Open **Postman**.
2. Click **New** > **gRPC Request**.
3. Enter the target server (e.g., `grpc://localhost:50051`).
4. Select the method, e.g., `ProductService / GetProduct`.

---

### 📄 Step 2: Import the `.proto` Files

1. Go to the **Service Definition** tab in Postman.
2. Click **Import a .proto file**.
3. Upload the main `.proto` files found in the following directory:

   ```
   \ve.net.alge.grpc\src\main\proto
   ```

   - ✅ This path contains proto definitions for both:
     - `UserService` (user.proto)
     - `ProductService` (product.proto)

---

### 📁 Step 3: Add Import Paths for Proto Dependencies

To properly resolve imported `.proto` files (like shared files or Google protobufs), you'll need to add all relevant import paths:

1. In the **Service Definition** tab, go to **Import paths**.
2. Click **+ Add Import Path** and include the following:

   #### ✅ Import Paths (All paths are from the `ve.net.alge.grpc` plugin)

   ```
   ve.net.alge.grpc\src\main\proto
   ve.net.alge.grpc\target\proto-dependencies\ce5781eccd28d0d1ae571cb0f9bf2e2f
   ve.net.alge.grpc\target\proto-dependencies\fa24a8af1c95adcd549a9ba7ae615f63f
   ve.net.alge.grpc\target\proto-dependencies\ff0fac2f3c693252fc9e8cce9a2bc5a
   ```

3. Ensure **all paths are checked ✅** for proper resolution.

---

### ▶️ Step 4: Invoke the gRPC Method

1. Compose your request message (JSON-style structure).
2. Click **Invoke**.
3. Observe the result in the **Response** panel.

---

### 🧾 Example Method

For invoking a method like `GetProduct`:

- Target: `grpc://localhost:50051`
- Service: `ProductService`
- Method: `GetProduct`
- Message Body:
  ```json
  {
    "productId": "12345"
  }
  ```

---
