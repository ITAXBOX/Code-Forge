# 🚀 Code Forge - The Future of Project Generation

**Code Forge** is a revolutionary full-stack project generator that transforms your ideas into production-ready code in a minute. Built with Spring Boot and featuring a sleek web interface, it automatically generates complete applications with backend APIs, frontend interfaces, and database configurations.

![Java](https://img.shields.io/badge/Java-22-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen?style=for-the-badge&logo=springboot)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template%20Engine-blue?style=for-the-badge&logo=thymeleaf)
![Maven](https://img.shields.io/badge/Maven-Build%20Tool-red?style=for-the-badge&logo=apachemaven)

## 🌟 Features

### 🎯 **Stack Code Generation**
- **Backend**: Spring Boot with JPA/Hibernate
- **Frontend**: Next.js
- **Database**: MySQL
- **Security**: Spring Security with JWT authentication
- **API**: RESTful endpoints with CRUD operations

### 🧠 **Intelligent Entity Management**
- **Visual Entity Builder**: Interactive web interface for defining entities
- **Attribute Management**: Support for various data types (String, Integer, Boolean, Date, etc.)
- **Relationship Handling**: One-to-Many, Many-to-One, Many-to-Many relationships
- **JSON Import**: Import entity structures from JSON files
- **Validation**: Built-in relationship and attribute validation

### 🎨 **Modern UI/UX**
- **Responsive Design**: Beautiful, mobile-first interface
- **Dark Theme**: Professional space-themed design with animated stars
- **Real-time Feedback**: Loading screens and progress indicators
- **Form Validation**: Client-side and server-side validation
- **Accessibility**: WCAG compliant interface

### 🏗️ **Project Structure Generation**
- **Complete Project Setup**: Maven/Gradle configuration
- **Layered Architecture**: Controllers, Services, Repositories, DTOs
- **Security Configuration**: Authentication and authorization setup
- **Database Configuration**: Connection pooling and JPA settings
- **Development Tools**: DevTools, Lombok integration

## 🛠️ Technology Stack

### Backend Technologies
- **Spring Boot 3.3.2**: Core framework with auto-configuration
- **Spring Web**: RESTful web services and MVC
- **Spring Data JPA**: Database abstraction layer
- **Spring Security**: Authentication and authorization
- **Thymeleaf**: Server-side template engine
- **Lombok**: Boilerplate code reduction
- **Maven**: Dependency management and build tool

### Frontend Technologies
- **Thymeleaf Templates**: Server-side rendering
- **CSS3**: Modern styling with CSS variables
- **JavaScript**: Interactive client-side functionality
- **Next.js Generation**: Automated React-based frontend creation

### Generated Project Technologies
- **Spring Boot**: Backend framework
- **JPA/Hibernate**: ORM for database operations
- **MySQL/PostgreSQL/MongoDB/SQLite**: Database options
- **JWT**: Token-based authentication
- **Next.js/React/Vue/Angular**: Frontend framework options
- **REST APIs**: RESTful web services

## 📁 Project Structure

```
Code Forge/
├── src/main/java/DrMuhamadMubarak/TheFuture/
│   ├── TheFutureApplication.java          # Main Spring Boot application
│   ├── generator/
│   │   ├── controller/
│   │   │   └── ProjectWorkflowController.java  # Main web controller
│   │   ├── service/
│   │   │   ├── EntityCodeGeneratorService.java # Entity generation logic
│   │   │   ├── ProjectStructureService.java    # Project structure creation
│   │   │   ├── FrontendService.java            # Frontend generation
│   │   │   ├── BehaviorService.java            # Custom behavior generation
│   │   │   ├── AttributeManagerService.java    # Attribute management
│   │   │   └── RelationshipValidator.java      # Relationship validation
│   │   ├── enums/
│   │   │   ├── FrontendType.java              # Frontend framework options
│   │   │   ├── BackendType.java               # Backend framework options
│   │   │   └── DatabaseType.java              # Database options
│   │   └── dto/
│   │       ├── AttributeDTO.java              # Attribute data transfer object
│   │       └── BehaviorGenerationResult.java  # Behavior generation result
│   ├── backend/springboot/
│   │   ├── SpringEntities.java               # Entity class generation
│   │   ├── SpringRepository.java             # Repository generation
│   │   ├── SpringService.java                # Service layer generation
│   │   ├── SpringController.java             # Controller generation
│   │   ├── SpringSecurity.java               # Security configuration
│   │   ├── SpringStructure.java              # Project structure
│   │   └── SpringDataInitializer.java        # Data initialization
│   ├── frontend/nextjs/
│   │   ├── NextjsGenerator.java              # Next.js project generation
│   │   ├── service/
│   │   │   ├── NextjsFileService.java        # File operations
│   │   │   ├── ApiGeneratorService.java      # API client generation
│   │   │   └── EntityExtractorService.java   # Entity extraction
│   │   └── metadata/
│   │       ├── EntityInfo.java               # Entity metadata
│   │       ├── EntityAttributeInfo.java      # Attribute metadata
│   │       └── EndpointInfo.java             # API endpoint metadata
│   └── utils/
│       ├── StringUtils.java                  # String manipulation utilities
│       ├── FileUtils.java                    # File operation utilities
│       ├── TokenUtils.java                   # Token generation utilities
│       └── EntityContext.java                # Entity context management
├── src/main/resources/
│   ├── templates/                            # Thymeleaf templates
│   │   ├── index.html                        # Landing page
│   │   ├── start.html                        # Project configuration
│   │   ├── entities.html                     # Entity definition
│   │   ├── add-attributes.html               # Attribute management
│   │   ├── result.html                       # Generation results
│   │   ├── loading-page.html                 # Loading interface
│   │   ├── leaderboard.html                  # User statistics
│   │   ├── error.html                        # Error page
│   │   ├── legal/                           # Legal pages
│   │   │   ├── terms.html                    # Terms of service
│   │   │   ├── privacy.html                  # Privacy policy
│   │   │   └── licenses.html                 # License information
│   │   └── resources/
│   │       └── support.html                  # Support page
│   ├── static/                              # Static assets
│   │   ├── favicon.ico                       # Website icon
│   │   └── logo/                            # Logo assets
│   └── application.properties               # Application configuration
├── Code Forge Dashboard/                    # Next.js template project
│   ├── app/                                # Next.js 13+ app directory
│   │   ├── layout.tsx                       # Root layout
│   │   ├── page.tsx                         # Home page
│   │   ├── globals.css                      # Global styles
│   │   ├── dashboard/                       # Dashboard pages
│   │   ├── login/                          # Authentication pages
│   │   └── register/
│   ├── components/                         # Reusable components
│   │   ├── ui/                             # UI component library
│   │   └── auth/                           # Authentication components
│   ├── lib/                               # Utility libraries
│   ├── hooks/                             # Custom React hooks
│   └── public/                            # Public assets
└── Projects/                              # Generated projects output directory
```

## 🚀 Getting Started

### Prerequisites
- **Java 22** or higher
- **Maven 3.6+**
- **Git** for version control
- **Node.js 18+** (for generated Next.js projects)

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/DrMuhamadMubarak/CodeForge.git
cd CodeForge
```

2. **Build the project:**
```bash
mvn clean install
```

3. **Run the application:**
```bash
mvn spring-boot:run
```

4. **Access the application:**
Open your browser and navigate to `http://localhost:8080`

## 📖 How to Use

### 1. **Project Configuration**
- Navigate to the home page and click "Start Your Journey"
- Choose your technology stack:
  - **Frontend**: Next.js (more coming soon)
  - **Backend**: Spring Boot (more coming soon)
  - **Database**: MySQL (more coming soon)
- Enter your project name

### 2. **Entity Definition**
Choose between the three methods:

#### **Interactive Method:**
- Define entities one by one using the web interface
- Add attributes with specific data types
- Configure relationships between entities
- Add custom behaviors and methods

#### **JSON Import Method:**
```json
{
  "entities": [
    {
      "name": "User",
      "attributes": [
        {"name": "id", "type": "Long", "primaryKey": true},
        {"name": "username", "type": "String", "unique": true},
        {"name": "email", "type": "String", "unique": true},
        {"name": "password", "type": "String"},
        {"name": "createdAt", "type": "LocalDateTime"}
      ],
      "relationships": [
        {"type": "OneToMany", "target": "Post", "mappedBy": "author"}
      ]
    },
    {
      "name": "Post",
      "attributes": [
        {"name": "id", "type": "Long", "primaryKey": true},
        {"name": "title", "type": "String"},
        {"name": "content", "type": "String"},
        {"name": "publishedAt", "type": "LocalDateTime"}
      ],
      "relationships": [
        {"type": "ManyToOne", "target": "User", "joinColumn": "author_id"}
      ]
    }
  ]
}
```

### 3. **Code Generation**
The system automatically generates:

#### **Backend (Spring Boot):**
- **Entity Classes**: JPA entities with Lombok annotations
- **Repository Interfaces**: Spring Data JPA repositories
- **Service Classes**: Business logic layer
- **Controllers**: REST API endpoints
- **DTOs**: Data transfer objects
- **Security Configuration**: JWT-based authentication
- **Database Configuration**: Connection and JPA settings

#### **Frontend (Next.js/React/Vue/Angular):**
- **Components**: CRUD operation components
- **API Clients**: HTTP service classes
- **Pages**: List, create, edit, delete pages
- **Routing**: Navigation configuration
- **State Management**: Context or store setup

## 🎨 Generated Code Examples

### Entity Class (Spring Boot)
```java
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();
}
```

### Repository Interface
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### REST Controller
```java
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(user));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Next.js Component
```tsx
'use client';

import { useState, useEffect } from 'react';
import { User } from '@/types/User';
import { userApi } from '@/lib/api/userApi';

export default function UserList() {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            const data = await userApi.getAll();
            setUsers(data);
        } catch (error) {
            console.error('Error loading users:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        try {
            await userApi.delete(id);
            setUsers(users.filter(user => user.id !== id));
        } catch (error) {
            console.error('Error deleting user:', error);
        }
    };

    if (loading) return <div>Loading...</div>;

    return (
        <div className="user-list">
            <h1>Users</h1>
            {users.map(user => (
                <div key={user.id} className="user-card">
                    <h3>{user.username}</h3>
                    <p>{user.email}</p>
                    <button onClick={() => handleDelete(user.id)}>
                        Delete
                    </button>
                </div>
            ))}
        </div>
    );
}
```

## 🔧 Configuration

### Application Properties
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration (Generated Projects)
spring.datasource.url=jdbc:mysql://localhost:3306/${project_name}
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Security Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Maven Dependencies (Generated Projects)
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Database Driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

## 🌐 API Reference

### Core Endpoints

#### **Project Generation**
- `GET /` - Landing page
- `GET /start` - Project configuration page
- `POST /process-project-info` - Process project configuration
- `GET /entities` - Entity definition page
- `POST /generate-entities` - Generate entities from form
- `POST /generate-entities-from-json` - Generate entities from JSON

#### **Attribute Management**
- `GET /add-attributes` - Attribute management page
- `POST /add-attributes` - Add attributes to entity
- `POST /next-entity` - Move to next entity
- `POST /finish-entity` - Complete entity definition

#### **Project Completion**
- `GET /result` - Generation results page
- `GET /loading-page` - Loading interface

#### **Utility Pages**
- `GET /leaderboard` - Usage statistics
- `GET /legal/terms` - Terms of service
- `GET /legal/privacy` - Privacy policy
- `GET /legal/licenses` - License information
- `GET /resources/support` - Support page

## 🎯 Supported Data Types

### **Primitive Types**
- `String` - Text data
- `Integer` / `int` - Whole numbers
- `Long` / `long` - Large whole numbers
- `Double` / `double` - Decimal numbers
- `Float` / `float` - Floating-point numbers
- `Boolean` / `boolean` - True/false values

### **Date/Time Types**
- `LocalDate` - Date without time
- `LocalDateTime` - Date with time
- `LocalTime` - Time without date
- `Instant` - Timestamp

### **Advanced Types**
- `BigDecimal` - Precise decimal calculations
- `UUID` - Unique identifiers
- `Enum` - Enumerated values
- `Blob` - Binary large objects
- `Clob` - Character large objects

### **Collection Types**
- `List<T>` - Ordered collections
- `Set<T>` - Unique collections
- `Map<K,V>` - Key-value pairs

## 🔗 Relationship Types

### **One-to-One**
```java
@OneToOne
@JoinColumn(name = "profile_id")
private UserProfile profile;
```

### **One-to-Many**
```java
@OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
private List<Post> posts = new ArrayList<>();
```

### **Many-to-One**
```java
@ManyToOne
@JoinColumn(name = "author_id")
private User author;
```

### **Many-to-Many**
```java
@ManyToMany
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

## 🔒 Security Features

### **Authentication**
- JWT token-based authentication
- User registration and login
- Password encryption with BCrypt
- Token expiration and refresh

### **Authorization**
- Role-based access control (RBAC)
- Method-level security
- Endpoint protection
- CORS configuration

### **Security Configuration**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## 📊 Performance Features

### **Database Optimization**
- Connection pooling
- Query optimization
- Lazy loading
- Caching support

### **Frontend Optimization**
- Code splitting
- Tree shaking
- Bundle optimization
- Progressive loading

### **API Optimization**
- Pagination support
- Filtering and sorting
- Response compression
- Rate limiting

## 🔧 Customization

### **Template Customization**
The generator uses customizable templates for code generation. You can modify templates in:
- `src/main/java/DrMuhamadMubarak/TheFuture/backend/springboot/`
- `Code Forge Dashboard/` for frontend templates

## 🐛 Troubleshooting

### **Common Issues**

#### **Port Already in Use**
```bash
# Kill process using port 8080
lsof -ti:8080 | xargs kill -9
```

#### **Database Connection Issues**
- Verify database is running
- Check connection URL and credentials
- Ensure database exists

#### **Maven Build Issues**
```bash
# Clean and rebuild
mvn clean install -U
```

#### **Frontend Build Issues**
```bash
# Clear npm cache
npm cache clean --force
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### **Development Guidelines**
- Follow Java coding conventions
- Write comprehensive tests
- Update documentation
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Ali Itawi**
- GitHub: [@ITAXBOX](https://github.com/ITAXBOX)
- Email: aliitawi7@gmail.com

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Thymeleaf community for the template engine
- Lombok project for reducing boilerplate code
- All contributors who made this project possible

## 📞 Support

If you encounter any issues or have questions:

1. **Check the documentation** above
2. **Search existing issues** on GitHub
3. **Create a new issue** with detailed information
4. **Visit our support page** at `/resources/support`
---

**Code Forge** - *Transforming Ideas into Code, One Click at a Time* 🚀

*Built with ❤️ by Ali Itawi*
