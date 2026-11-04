# 📖 The OllamaSprout Engineering Story: GenAI Enterprise Bootstrapper

> **From Prompt to Production: How I built an automated, enterprise-grade architecture accelerator using Java 21, Spring Boot 3.x, Spring AI, and local LLMs.**

---

## 🚀 The Day 1 Microservice Friction

We all know the pain of starting a new corporate microservice. The first day is usually completely wasted on repetitive, non-differentiated heavy lifting:
* Setting up multi-module Maven trees
* Copy-pasting JPA entities
* Writing boilerplate DTOs
* Wiring local database configurations

As technology leaders, our focus should always remain on driving development velocity and eliminating structural boilerplate. I wanted to eliminate this friction entirely.

That is why I built **OllamaSprout**—an automated accelerator that translates a simple natural language prompt containing your domain properties into a fully compliant microservice that works out-of-the-box.

🔒 **The best part? It runs 100% locally. No cloud API keys, no data leaks, and absolute source privacy for strict enterprise compliance.**

---

## 🛠️ Step-by-Step Production Run

Here is the exact end-to-end journey of how I prepared the environment, executed the application, and generated production-ready code with absolute data privacy.

### Step 1: Environment Preparation & Ollama Setup (Windows 11)
To ensure absolute source code privacy and total regulatory compliance for sensitive enterprise requirements, this architecture bypasses cloud APIs entirely. Everything runs locally on the developer's workstation.

1. **Installing Ollama:** Downloaded the native Windows installer directly from the official Ollama website and ran the executable. The installation seamlessly configures the background daemon and exposes the command-line interface via PowerShell/Command Prompt.
2. **Pulling the Code Engine:** Open your command prompt and download the specialized model:
   ```bash
   ollama pull qwen2.5-coder:7b
   ```
3. **Verifying the Infrastructure:** Once installed, run the verification command in the Command Prompt to check the locally available inventory:
   ```bash
   ollama list
   ```

### Step 2: Spring AI Integration
With the local inference engine verified and running on port `11434`, map the runtime connection inside the bootstrapper's `application.properties` file:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen2.5-coder:7b
```

### Step 3: Booting the Application
Run the bootstrapper application natively inside the Eclipse IDE. The embedded web server binds to the local environment and prepares the AI orchestration layer.

Launch the browser interface by navigating to:
👉 `http://localhost:8081`

---

## 🧠 The Microservice Blueprint Execution

Within the UI workspace dashboard, follow these inputs:

1. **Set Storage Path:** Place your workspace directory in the text box:
   `C:/Kani/eclipse-workspace/product-service`
2. **Inject the Blueprint Prompt:** Paste the following strict instructions into the prompt box:

```text
Create a complete, working Spring Boot CRUD web application for a "Product" catalog utilizing Java 21, Spring Data JPA, and an H2 In-Memory Database. 

The application must be contained entirely within the base package structure: com.kani

Generate the following 7 discrete files using the required structural tags:

[FILE: pom.xml]
<!-- 
Instructions: Generate a clean Maven pom.xml configuration targeting Java 21.

CRITICAL ECLIPSE OFFLINE VALIDATION RULE: You MUST format the root project tag exactly as shown below using the 'http' schema variation. This ensures Eclipse's built-in offline cache can validate the descriptor when external downloads are disabled:
<project xmlns="http://apache.org" xmlns:xsi="http://w3.org" xsi:schemaLocation="http://apache.org http://apache.org">

CRITICAL COMPATIBILITY RULE: Automatically analyze the target Java version (21) and dynamically set the Spring Boot parent starter version to a fully compatible tier (3.2.x or higher, such as 3.3.5). Do not use deprecated tiers like 3.0.0. Include starters for: spring-boot-starter-web, spring-boot-starter-data-jpa, and com.h2database:h2 (runtime scope). Include the standard spring-boot-maven-plugin.
-->


[FILE: src/main/resources/application.properties]
<!-- 
Instructions: Provide production-ready configurations for an H2 In-Memory Database. Enable the H2 web console, set the datasource URL to 'jdbc:h2:mem:testdb', set username to 'sa', leave password empty, configure the H2Dialect, set the server port to 8080, and set the hibernate ddl-auto strategy to 'update'.
-->

[FILE: src/main/java/com/kani/SimpleCrudOperationApplication.java]
<!-- 
Instructions: Synthesize the main entrypoint class inside the com.kani package. Annotate it with @SpringBootApplication and include the public static void main method that executes SpringApplication.run().
-->

[FILE: src/main/java/com/kani/model/Product.java]


[FILE: src/main/java/com/kani/repository/ProductRepository.java]


[FILE: src/main/java/com/kani/service/ProductService.java]
<!-- 
Instructions: Generate a transaction-ready service class inside com.kani.service annotated with @Service. Implement clean constructor injection to pull in the ProductRepository dependency. Write out complete, production-grade business logic methods for: getAllProducts(), getProductById(Long id), createProduct(Product product), updateProduct(Long id, Product product), and deleteProduct(Long id).
-->

[FILE: src/main/java/com/kani/controller/ProductController.java]
<!-- 
Instructions: Generate a comprehensive REST controller class inside com.kani.controller annotated with @RestController and mapped to base path "/api/products". Implement full REST endroute mappings returning proper ResponseEntity payloads: @GetMapping (all items), @GetMapping("/{id}") (individual lookup returning 404 if missing), @PostMapping (creation returning 201 Created), @PutMapping("/{id}") (update operation), and @DeleteMapping("/{id}") (deletion returning 204 No Content).
-->

CRITICAL ASSIGNMENT INSTRUCTIONS:
1. Provide fully written, production-ready code blocks inside every file section.
2. Absolutely no code shortcuts, no '// TODO' markers, and no trailing ellipses '...' are permitted.
3. Every file block must map strictly to the isolated path listed in its header tag.
```

Click **Execute Blueprint Code Generation**. OllamaSprout will stream the inference, parse the blocks, and automatically write the complete Maven directory tree structure directly to your disk.

---

## 🧪 Import & Live Validation in Eclipse

Import the generated project into Eclipse as an *Existing Maven Project*. The codebase resolves immediately with **zero errors, zero warnings, and completely intact package configurations**.

### 1. Launching the Scaffolding
Run the generated project as a Spring Boot App. The console logs track the system spinning up smoothly on port `8080` and establishing an In-Memory H2 catalog instance.

### 2. Database Verification
Open your browser and navigate to the embedded console:
👉 `http://localhost:8080/h2-console`

Log in using `jdbc:h2:mem:testdb` with username `sa` to verify that Hibernate has automatically analyzed the domain layer and built a clean, fresh `PRODUCT` database schema.

### 3. Testing the REST Lifecycle via Terminal
To prove the generated microservice handles live HTTP traffic, open a terminal window and run a `curl` command to insert your first enterprise record:

```bash
curl -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d "{"name":"Enterprise Server Pro","price":125000.00,"stock":15}"
```

---

## 📌 Architectural Takeaways & Next Steps
* **Fresh Context Starts:** Because it utilizes an In-Memory H2 database, database states refresh on every application restart.
* **Persistent Growth:** Want local drive persistence or a containerized PostgreSQL environment? Simply adjust your natural language prompt parameters when executing the blueprint generation, and the local LLM will auto-wire the configurations for you seamlessly.
