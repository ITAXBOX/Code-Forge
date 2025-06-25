"use client"

import { useEffect, useState, useRef } from "react"
import {
  ArrowLeft,
  Code,
  Database,
  Edit,
  Eye,
  FileCode,
  LogIn,
  LogOut,
  RefreshCw,
  Save,
  Search,
  Trash2,
  UserPlus,
} from "lucide-react"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Separator } from "@/components/ui/separator"

interface EntityData {
  id?: number
  name: string
  description?: string
  [key: string]: any
}

export default function Dashboard() {
  const [currentPage, setCurrentPage] = useState<"home" | "login" | "register" | "dashboard">("home")
  const [selectedEntity, setSelectedEntity] = useState<string | null>(null)
  const [selectedTab, setSelectedTab] = useState<"list" | "create" | "detail" | "update" | "delete" | "custom">("list")
  const [isLoading, setIsLoading] = useState(true)
  const [currentTime, setCurrentTime] = useState(() => new Date())
  const [mounted, setMounted] = useState(false)

  // Entity data state
  const [entityData, setEntityData] = useState<EntityData[]>([])
  const [selectedEntityData, setSelectedEntityData] = useState<EntityData | null>(null)
  const [isEntityLoading, setIsEntityLoading] = useState(false)
  const [entityError, setEntityError] = useState("")
  const [entitySuccess, setEntitySuccess] = useState("")

  // Form states
  const [createForm, setCreateForm] = useState<EntityData>({ name: "", description: "" })
  const [updateForm, setUpdateForm] = useState<EntityData>({ name: "", description: "" })
  const [searchQuery, setSearchQuery] = useState("")

  // Auth form state
  const [loginEmail, setLoginEmail] = useState("")
  const [loginPassword, setLoginPassword] = useState("")
  const [registerEmail, setRegisterEmail] = useState("")
  const [registerUsername, setRegisterUsername] = useState("")
  const [registerPassword, setRegisterPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [authMessage, setAuthMessage] = useState("")
  const [authError, setAuthError] = useState("")
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // API base URLs
  const AUTH_API_BASE_URL = "http://localhost:8081/api/auth"
  const API_BASE_URL = "http://localhost:8081/api"

  // Get API endpoint for selected entity
  const getEntityEndpoint = () => {
    if (!selectedEntity) return ""
    return `${API_BASE_URL}/${selectedEntity.toLowerCase()}s`
  }

  // Function to handle login
  const handleLogin = async () => {
    if (!loginEmail || !loginPassword) {
      setAuthError("Please fill in all fields")
      return
    }

    setIsSubmitting(true)
    setAuthError("")
    setAuthMessage("")

    try {
      const response = await fetch(`${AUTH_API_BASE_URL}/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: loginEmail,
          password: loginPassword,
        }),
        credentials: "include",
      })

      if (response.ok) {
        setAuthMessage("Login successful!")
        setIsAuthenticated(true)
        setCurrentPage("dashboard")
      } else {
        setAuthError("Email or password is incorrect")
      }
    } catch (error) {
      console.error("Login error:", error)
      setAuthError("Failed to connect to the server. Please try again.")
    } finally {
      setIsSubmitting(false)
    }
  }

  // Function to handle registration
  const handleRegister = async () => {
    if (!registerUsername || !registerEmail || !registerPassword || !confirmPassword) {
      setAuthError("Please fill in all fields")
      return
    }

    if (registerPassword !== confirmPassword) {
      setAuthError("Passwords do not match")
      return
    }

    setIsSubmitting(true)
    setAuthError("")
    setAuthMessage("")

    try {
      const response = await fetch(`${AUTH_API_BASE_URL}/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username: registerUsername,
          email: registerEmail,
          password: registerPassword,
        }),
      })

      if (response.ok) {
        setAuthMessage("Registration successful! Please log in.")
        setCurrentPage("login")
      } else {
        const errorData = await response.text()
        setAuthError(errorData || "Registration failed. Please try again.")
      }
    } catch (error) {
      console.error("Registration error:", error)
      setAuthError("Failed to connect to the server. Please try again.")
    } finally {
      setIsSubmitting(false)
    }
  }

  // Function to handle logout
  const handleLogout = async () => {
    try {
      const response = await fetch(`${AUTH_API_BASE_URL}/logout`, {
        method: "POST",
        credentials: "include",
      })

      if (response.ok) {
        setIsAuthenticated(false)
        setCurrentPage("home")
        setSelectedEntity(null)
        setEntityData([])
      } else {
        console.error("Logout failed")
      }
    } catch (error) {
      console.error("Logout error:", error)
    }
  }

  // CRUD Operations
  const fetchEntityData = async () => {
    if (!selectedEntity) return

    setIsEntityLoading(true)
    setEntityError("")

    try {
      const response = await fetch(getEntityEndpoint(), {
        credentials: "include",
      })

      if (response.ok) {
        const data = await response.json()
        // Ensure data is an array and map the fields to match entity attributes if needed
        const formattedData = Array.isArray(data)
          ? data.map(item => {
              // For each item, ensure it has properties that match our expected attribute names
              const entityDef = entities.find(e => e.name === selectedEntity);
              if (entityDef) {
                // Create a normalized item with expected field names
                const normalizedItem = { ...item };

                // Handle cases where API returns differently named fields or nested objects
                entityDef.attributes.forEach(attr => {
                  // If the field doesn't exist directly, try to find it in a different case or nested object
                  if (normalizedItem[attr.name] === undefined) {
                    // Check for camelCase, snake_case and PascalCase variations
                    const pascalCase = attr.name.charAt(0).toUpperCase() + attr.name.slice(1);
                    const snakeCase = attr.name.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);

                    if (item[pascalCase] !== undefined) {
                      normalizedItem[attr.name] = item[pascalCase];
                    } else if (item[snakeCase] !== undefined) {
                      normalizedItem[attr.name] = item[snakeCase];
                    } else {
                      // Try to find in nested objects (common for relationships)
                      Object.keys(item).forEach(key => {
                        if (typeof item[key] === 'object' && item[key] !== null) {
                          if (item[key][attr.name] !== undefined) {
                            normalizedItem[attr.name] = item[key][attr.name];
                          }
                        }
                      });
                    }
                  }
                });
                return normalizedItem;
              }
              return item;
            })
          : [];

        setEntityData(formattedData);
      } else {
        setEntityError("Failed to fetch data")
      }
    } catch (error) {
      console.error("Fetch error:", error)
      setEntityError("Failed to connect to the server")
    } finally {
      setIsEntityLoading(false)
    }
  }

  const fetchEntityById = async (id: number) => {
    if (!selectedEntity) return

    setIsEntityLoading(true)
    setEntityError("")

    try {
      const response = await fetch(`${getEntityEndpoint()}/${id}`, {
        credentials: "include",
      })

      if (response.ok) {
        const data = await response.json()
        setSelectedEntityData(data)
        setUpdateForm(data)
      } else {
        setEntityError("Failed to fetch entity details")
      }
    } catch (error) {
      console.error("Fetch entity error:", error)
      setEntityError("Failed to connect to the server")
    } finally {
      setIsEntityLoading(false)
    }
  }

  const createEntity = async () => {
    if (!selectedEntity || !createForm.name) {
      setEntityError("Please fill in required fields")
      return
    }

    setIsEntityLoading(true)
    setEntityError("")
    setEntitySuccess("")

    try {
      const response = await fetch(getEntityEndpoint(), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(createForm),
        credentials: "include",
      })

      if (response.ok) {
        setEntitySuccess(`${selectedEntity} created successfully!`)
        setCreateForm({ name: "", description: "" })
        fetchEntityData() // Refresh the list
        setSelectedTab("list")
      } else {
        setEntityError("Failed to create entity")
      }
    } catch (error) {
      console.error("Create error:", error)
      setEntityError("Failed to connect to the server")
    } finally {
      setIsEntityLoading(false)
    }
  }

  const updateEntity = async () => {
    // Remove the check for selectedEntityData?.id since it's already set and unchangeable
    if (!selectedEntity) {
      setEntityError("Please select an entity to update")
      return
    }

    // Check if required fields are filled
    const requiredFields = entities.find(e => e.name === selectedEntity)?.attributes
      .filter(attr => attr.required && attr.name !== 'id') // Exclude ID from required fields check

    let hasEmptyRequiredField = false

    if (requiredFields && requiredFields.length > 0) {
      for (const field of requiredFields) {
        if (!updateForm[field.name]) {
          hasEmptyRequiredField = true
          break
        }
      }
    }

    if (hasEmptyRequiredField) {
      setEntityError("Please fill in all required fields")
      return
    }

    setIsEntityLoading(true)
    setEntityError("")
    setEntitySuccess("")

    // Ensure ID is included from selectedEntityData but isn't changed by user
    const dataToUpdate = {
      ...updateForm,
      id: selectedEntityData?.id // Make sure ID is set from the original data
    }

    try {
      const response = await fetch(`${getEntityEndpoint()}/${selectedEntityData?.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(dataToUpdate),
        credentials: "include",
      })

      if (response.ok) {
        setEntitySuccess(`${selectedEntity} updated successfully!`)
        fetchEntityData() // Refresh the list
        setSelectedTab("list")
      } else {
        const errorData = await response.text()
        setEntityError(errorData || "Failed to update entity")
      }
    } catch (error) {
      console.error("Update error:", error)
      setEntityError("Failed to connect to the server")
    } finally {
      setIsEntityLoading(false)
    }
  }

  const deleteEntity = async (id: number) => {
    if (!selectedEntity) return

    setIsEntityLoading(true)
    setEntityError("")
    setEntitySuccess("")

    try {
      const response = await fetch(`${getEntityEndpoint()}/${id}`, {
        method: "DELETE",
        credentials: "include",
      })

      if (response.ok) {
        setEntitySuccess(`${selectedEntity} deleted successfully!`)
        fetchEntityData() // Refresh the list
        setSelectedTab("list")
      } else {
        setEntityError("Failed to delete entity")
      }
    } catch (error) {
      console.error("Delete error:", error)
      setEntityError("Failed to connect to the server")
    } finally {
      setIsEntityLoading(false)
    }
  }

  const canvasRef = useRef<HTMLCanvasElement>(null)

  // Mock entities data
  const entities = [ ]

  // Simulate data loading
  useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false)
    }, 2000)

    return () => clearTimeout(timer)
  }, [])

  // Update time
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTime(new Date())
    }, 1000)

    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    setMounted(true)
  }, [])

  // Fetch data when entity is selected
  useEffect(() => {
    if (selectedEntity && selectedTab === "list") {
      fetchEntityData()
    }
  }, [selectedEntity, selectedTab])

  // Particle effect
  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext("2d")
    if (!ctx) return

    canvas.width = canvas.offsetWidth
    canvas.height = canvas.offsetHeight

    const particles: Particle[] = []
    const particleCount = 100

    class Particle {
      x: number
      y: number
      size: number
      speedX: number
      speedY: number
      color: string

      constructor() {
        // @ts-ignore
        this.x = Math.random() * canvas.width
        // @ts-ignore
        this.y = Math.random() * canvas.height
        this.size = Math.random() * 3 + 1
        this.speedX = (Math.random() - 0.5) * 0.5
        this.speedY = (Math.random() - 0.5) * 0.5
        this.color = `rgba(${Math.floor(Math.random() * 100) + 100}, ${
            Math.floor(Math.random() * 100) + 150
        }, ${Math.floor(Math.random() * 55) + 200}, ${Math.random() * 0.5 + 0.2})`
      }

      update() {
        this.x += this.speedX
        this.y += this.speedY

        // @ts-ignore
        if (this.x > canvas.width) this.x = 0
        if (this.x < 0) {
          // @ts-ignore
          this.x = canvas.width
        }
        // @ts-ignore
        if (this.y > canvas.height) this.y = 0
        if (this.y < 0) {
          // @ts-ignore
          this.y = canvas.height
        }
      }

      draw() {
        if (!ctx) return
        ctx.fillStyle = this.color
        ctx.beginPath()
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
        ctx.fill()
      }
    }

    for (let i = 0; i < particleCount; i++) {
      particles.push(new Particle())
    }

    function animate() {
      if (!ctx || !canvas) return
      ctx.clearRect(0, 0, canvas.width, canvas.height)

      for (const particle of particles) {
        particle.update()
        particle.draw()
      }

      requestAnimationFrame(animate)
    }

    animate()

    const handleResize = () => {
      if (!canvas) return
      canvas.width = canvas.offsetWidth
      canvas.height = canvas.offsetHeight
    }

    window.addEventListener("resize", handleResize)

    return () => {
      window.removeEventListener("resize", handleResize)
    }
  }, [])

  // Format time
  const formatTime = (date: Date) => {
    return date.toLocaleTimeString("en-US", {
      hour12: false,
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    })
  }

  // Format date
  const formatDate = (date: Date) => {
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  // Filter entity data based on search
  const filteredEntityData = entityData.filter(
    (item) => {
      // Special handling for User entity
      if (selectedEntity === "User") {
        return (
          (item.username && item.username.toLowerCase().includes(searchQuery.toLowerCase())) ||
          (item.email && item.email.toLowerCase().includes(searchQuery.toLowerCase()))
        );
      }

      // Default handling for other entities
      return (
        (item.name && item.name.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (item.description && item.description.toLowerCase().includes(searchQuery.toLowerCase()))
      );
    }
  )

  // Render home page
  const renderHomePage = () => (
      <div className="flex flex-col items-center justify-center h-[80vh] text-center">
        <div className="mb-8">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-cyan-500 to-blue-600 bg-clip-text text-transparent mb-2">
            ENTITY MANAGER
          </h1>
          <p className="text-gray-600 max-w-md mx-auto">
            Generate complete backend applications with AI, including entity definitions, CRUD endpoints, and custom
            behavior.
          </p>
        </div>

        <div className="flex space-x-4">
          <Button
              className="bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700 text-white px-8 py-6"
              onClick={() => setCurrentPage("login")}
          >
            <LogIn className="mr-2 h-5 w-5" />
            Login
          </Button>
          <Button
              variant="outline"
              className="border-cyan-500 text-cyan-600 hover:bg-cyan-50 px-8 py-6"
              onClick={() => setCurrentPage("register")}
          >
            <UserPlus className="mr-2 h-5 w-5" />
            Register
          </Button>
        </div>

        <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl">
          <FeatureCard
              icon={Database}
              title="Data Management"
              description="Easily manage and organize your application data"
          />
          <FeatureCard
              icon={RefreshCw}
              title="API Integration"
              description="Connect with powerful APIs and external services"
          />
          <FeatureCard
              icon={FileCode}
              title="User Authentication"
              description="Secure access control for your application users"
          />
        </div>
      </div>
  )

  // Render login page
  const renderLoginPage = () => (
      <div className="flex justify-center items-center h-[80vh]">
        <Card className="w-full max-w-md bg-white border-gray-200 backdrop-blur-sm shadow-lg">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl text-center bg-gradient-to-r from-cyan-500 to-blue-600 bg-clip-text text-transparent">
              Login to Code Forge
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                  id="email"
                  placeholder="your.email@example.com"
                  className="bg-gray-50 border-gray-300 focus:border-cyan-500"
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Password</Label>
              </div>
              <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  className="bg-gray-50 border-gray-300 focus:border-cyan-500"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button
                className="w-full bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700"
                onClick={handleLogin}
                disabled={isSubmitting}
            >
              {isSubmitting ? "Logging in..." : "Login"}
            </Button>
            {authMessage && <div className="text-center text-sm text-green-600">{authMessage}</div>}
            {authError && <div className="text-center text-sm text-red-600">{authError}</div>}
            <div className="text-center text-sm text-gray-500">
              Don't have an account?{" "}
              <Button variant="link" className="text-cyan-600 p-0" onClick={() => setCurrentPage("register")}>
                Register
              </Button>
            </div>
            <Button
                variant="outline"
                className="w-full border-gray-300 text-gray-600 hover:bg-gray-50"
                onClick={() => setCurrentPage("home")}
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Home
            </Button>
          </CardFooter>
        </Card>
      </div>
  )

  // Render register page
  const renderRegisterPage = () => (
      <div className="flex justify-center items-center h-[80vh]">
        <Card className="w-full max-w-md bg-white border-gray-200 backdrop-blur-sm shadow-lg">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl text-center bg-gradient-to-r from-cyan-500 to-blue-600 bg-clip-text text-transparent">
              Create an Account
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                  id="email"
                  placeholder="your.email@example.com"
                  className="bg-gray-50 border-gray-300 focus:border-cyan-500"
                  value={registerEmail}
                  onChange={(e) => setRegisterEmail(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                  id="username"
                  placeholder="Enter a username"
                  className="bg-gray-50 border-gray-300 focus:border-cyan-500"
                  value={registerUsername}
                  onChange={(e) => setRegisterUsername(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  className="bg-gray-50 border-gray-300 focus:border-cyan-500"
                  value={registerPassword}
                  onChange={(e) => setRegisterPassword(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirm-password">Confirm Password</Label>
              <Input
                  id="confirm-password"
                  type="password"
                  placeholder="••••••••"
                  className="bg-gray-50 border-gray-300 focus:border-cyan-500"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button
                className="w-full bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700"
                onClick={handleRegister}
                disabled={isSubmitting}
            >
              {isSubmitting ? "Registering..." : "Register"}
            </Button>
            {authMessage && <div className="text-center text-sm text-green-600">{authMessage}</div>}
            {authError && <div className="text-center text-sm text-red-600">{authError}</div>}
            <div className="text-center text-sm text-gray-500">
              Already have an account?{" "}
              <Button variant="link" className="text-cyan-600 p-0" onClick={() => setCurrentPage("login")}>
                Login
              </Button>
            </div>
            <Button
                variant="outline"
                className="w-full border-gray-300 text-gray-600 hover:bg-gray-50"
                onClick={() => setCurrentPage("home")}
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Home
            </Button>
          </CardFooter>
        </Card>
      </div>
  )

  // Render dashboard
  const renderDashboard = () => (
      <div className="grid grid-cols-12 gap-6">
        {/* Sidebar */}
        <div className="col-span-12 md:col-span-3 lg:col-span-2">
          <Card className="bg-white border-gray-200 backdrop-blur-sm h-full shadow-sm">
            <CardContent className="p-4">
              <div className="mb-6">
                <div className="text-xs text-gray-500 mb-2 font-mono">PROJECT</div>
                <div className="text-sm font-medium text-gray-900 mb-1">E-Commerce API</div>
                <div className="text-xs text-gray-500">Spring Boot / Java</div>
              </div>

              <Separator className="my-4 bg-gray-200" />

              <div className="mb-4">
                <div className="text-xs text-gray-500 mb-2 font-mono">ENTITIES</div>
                <nav className="space-y-1">
                  {entities.map((entity) => (
                      <Button
                          key={entity.name}
                          variant="ghost"
                          className={`w-full justify-start ${
                              selectedEntity === entity.name
                                  ? "bg-cyan-50 text-cyan-600"
                                  : "text-gray-600 hover:text-gray-900 hover:bg-gray-50"
                          }`}
                          onClick={() => {
                            setSelectedEntity(entity.name)
                            setSelectedTab("list")
                            setEntityError("")
                            setEntitySuccess("")
                          }}
                      >
                        <Database className="mr-2 h-4 w-4" />
                        {entity.name}
                      </Button>
                  ))}
                </nav>
              </div>

              <Separator className="my-4 bg-gray-200" />

              <div>
                <div className="text-xs text-gray-500 mb-2 font-mono">ACTIONS</div>
                <nav className="space-y-1">
                  <Button
                      variant="ghost"
                      className="w-full justify-start text-gray-600 hover:text-gray-900 hover:bg-gray-50"
                      onClick={handleLogout}
                  >
                    <LogOut className="mr-2 h-4 w-4" />
                    Logout
                  </Button>
                </nav>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Main content */}
        <div className="col-span-12 md:col-span-9 lg:col-span-10">
          {selectedEntity ? (
              <Card className="bg-white border-gray-200 backdrop-blur-sm shadow-sm">
                <CardHeader className="border-b border-gray-200 pb-3">
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-gray-900 flex items-center">
                      <Database className="mr-2 h-5 w-5 text-cyan-600" />
                      {selectedEntity} Entity
                    </CardTitle>
                    <div className="flex items-center space-x-2">
                      <Badge variant="outline" className="bg-cyan-50 text-cyan-600 border-cyan-200 text-xs">
                        API v1.0
                      </Badge>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="p-6">
                  {/* Success/Error Messages */}
                  {entitySuccess && (
                      <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-md text-green-700 text-sm">
                        {entitySuccess}
                      </div>
                  )}
                  {entityError && (
                      <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md text-red-700 text-sm">
                        {entityError}
                      </div>
                  )}

                  <Tabs
                      defaultValue="list"
                      value={selectedTab}
                      onValueChange={(value) => {
                        setSelectedTab(value as any)
                        setEntityError("")
                        setEntitySuccess("")
                      }}
                  >
                    <TabsList className="bg-gray-100 p-1 mb-6">
                      <TabsTrigger value="list" className="data-[state=active]:bg-white data-[state=active]:text-cyan-600">
                        List
                      </TabsTrigger>
                      <TabsTrigger
                          value="create"
                          className="data-[state=active]:bg-white data-[state=active]:text-cyan-600"
                      >
                        Create
                      </TabsTrigger>
                      <TabsTrigger
                          value="detail"
                          className="data-[state=active]:bg-white data-[state=active]:text-cyan-600"
                      >
                        Detail
                      </TabsTrigger>
                      <TabsTrigger
                          value="update"
                          className="data-[state=active]:bg-white data-[state=active]:text-cyan-600"
                      >
                        Update
                      </TabsTrigger>
                      <TabsTrigger
                          value="delete"
                          className="data-[state=active]:bg-white data-[state=active]:text-cyan-600"
                      >
                        Delete
                      </TabsTrigger>
                      <TabsTrigger
                          value="custom"
                          className="data-[state=active]:bg-white data-[state=active]:text-cyan-600"
                      >
                        Custom
                      </TabsTrigger>
                    </TabsList>

                    <TabsContent value="list" className="mt-0">
                      <div className="flex justify-between items-center mb-4">
                        <div className="text-sm text-gray-600">
                          Endpoint: <span className="text-cyan-600 font-mono">GET {getEntityEndpoint()}</span>
                        </div>
                        <div className="flex space-x-2">
                          <div className="relative">
                            <Search className="absolute left-2 top-2.5 h-4 w-4 text-gray-400" />
                            <Input
                                placeholder="Search..."
                                className="pl-8 bg-gray-50 border-gray-300 w-64"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                          </div>
                          <Button
                              variant="outline"
                              className="border-gray-300 text-gray-600"
                              onClick={fetchEntityData}
                              disabled={isEntityLoading}
                          >
                            <RefreshCw className={`h-4 w-4 ${isEntityLoading ? "animate-spin" : ""}`} />
                          </Button>
                        </div>
                      </div>

                      <div className="bg-gray-50 rounded-lg border border-gray-200 overflow-hidden">
                        <div className="grid grid-cols-12 text-xs text-gray-600 p-3 border-b border-gray-200 bg-gray-100">
                          {/* Show only entity attributes as column headers */}
                          {selectedEntity && entities.find(e => e.name === selectedEntity)?.attributes.map((attr, index) => (
                            <div key={attr.name} className={`col-span-${index === 0 ? 3 : 2}`}>
                              {attr.name.charAt(0).toUpperCase() + attr.name.slice(1)}
                            </div>
                          ))}
                          <div className="col-span-2 text-right">Actions</div>
                        </div>

                        <div className="divide-y divide-gray-200">
                          {isEntityLoading ? (
                              <div className="p-8 text-center text-gray-500">Loading...</div>
                          ) : filteredEntityData.length === 0 ? (
                              <div className="p-8 text-center text-gray-500">No {selectedEntity.toLowerCase()}s found</div>
                          ) : (
                              filteredEntityData.map((item) => (
                                  <div key={item.id} className="grid grid-cols-12 py-3 px-3 text-sm hover:bg-gray-50">
                                    {/* Show only entity attributes as row data */}
                                    {selectedEntity && entities.find(e => e.name === selectedEntity)?.attributes.map((attr, index) => (
                                      <div key={attr.name} className={`col-span-${index === 0 ? 3 : 2} text-gray-900 truncate`}>
                                        {item[attr.name] !== undefined && item[attr.name] !== null
                                          ? String(item[attr.name])
                                          : `No ${attr.name}`}
                                      </div>
                                    ))}
                                    <div className="col-span-2 text-right space-x-1">
                                      <Button
                                          variant="ghost"
                                          size="icon"
                                          className="h-7 w-7 text-gray-600"
                                          onClick={() => {
                                            fetchEntityById(item.id!)
                                            setSelectedTab("detail")
                                          }}
                                      >
                                        <Eye className="h-4 w-4" />
                                      </Button>
                                      <Button
                                          variant="ghost"
                                          size="icon"
                                          className="h-7 w-7 text-gray-600"
                                          onClick={() => {
                                            fetchEntityById(item.id!)
                                            setSelectedTab("update")
                                          }}
                                      >
                                        <Edit className="h-4 w-4" />
                                      </Button>
                                      <Button
                                          variant="ghost"
                                          size="icon"
                                          className="h-7 w-7 text-gray-600"
                                          onClick={() => {
                                            setSelectedEntityData(item)
                                            setSelectedTab("delete")
                                          }}
                                      >
                                        <Trash2 className="h-4 w-4" />
                                      </Button>
                                    </div>
                                  </div>
                              ))
                          )}
                        </div>
                      </div>

                      <div className="flex justify-between items-center mt-4">
                        <div className="text-sm text-gray-500">Showing {filteredEntityData.length} items</div>
                      </div>
                    </TabsContent>

                    <TabsContent value="create" className="mt-0">
                      <div className="text-sm text-gray-600 mb-4">
                        Endpoint: <span className="text-cyan-600 font-mono">POST {getEntityEndpoint()}</span>
                      </div>

                      <div className="bg-gray-50 rounded-lg border border-gray-200 p-6">
                        <div className="space-y-4">
                          {/* Dynamically generate form fields based on entity attributes */}
                          {selectedEntity && entities.find(e => e.name === selectedEntity)?.attributes.map((attr) => (
                            // Skip the ID field in create form since it should be auto-generated
                            attr.name !== 'id' && (
                              <div key={attr.name} className="space-y-2">
                                <Label htmlFor={`create-${attr.name}`}>{attr.name.charAt(0).toUpperCase() + attr.name.slice(1)} {attr.required && '*'}</Label>
                                {attr.type === 'text' || attr.type === 'textarea' ? (
                                  <Textarea
                                    id={`create-${attr.name}`}
                                    placeholder={`Enter ${attr.name}`}
                                    className="bg-white border-gray-300 min-h-[100px]"
                                    value={createForm[attr.name] || ''}
                                    onChange={(e) => setCreateForm({ ...createForm, [attr.name]: e.target.value })}
                                  />
                                ) : (
                                  <Input
                                    id={`create-${attr.name}`}
                                    placeholder={`Enter ${attr.name}`}
                                    className="bg-white border-gray-300"
                                    value={createForm[attr.name] || ''}
                                    onChange={(e) => setCreateForm({ ...createForm, [attr.name]: e.target.value })}
                                    type={attr.type === 'number' ? 'number' : 'text'}
                                  />
                                )}
                              </div>
                            )
                          ))}
                        </div>

                        <div className="mt-6 flex justify-end space-x-2">
                          <Button
                              variant="outline"
                              className="border-gray-300 text-gray-600"
                              onClick={() => {
                                // Reset form with empty values for all fields
                                const emptyForm = {};
                                if (selectedEntity) {
                                  entities.find(e => e.name === selectedEntity)?.attributes.forEach(attr => {
                                    if (attr.name !== 'id') { // Skip id field in reset
                                      emptyForm[attr.name] = '';
                                    }
                                  });
                                }
                                setCreateForm(emptyForm as EntityData);
                              }}
                          >
                            Clear
                          </Button>
                          <Button
                              className="bg-gradient-to-r from-cyan-500 to-blue-600"
                              onClick={createEntity}
                              disabled={isEntityLoading}
                          >
                            <Save className="mr-2 h-4 w-4" />
                            {isEntityLoading ? "Creating..." : `Create ${selectedEntity}`}
                          </Button>
                        </div>
                      </div>
                    </TabsContent>

                    <TabsContent value="detail" className="mt-0">
                      <div className="text-sm text-gray-600 mb-4">
                        Endpoint:{" "}
                        <span className="text-cyan-600 font-mono">
                      GET {getEntityEndpoint()}/{selectedEntityData?.id}
                    </span>
                      </div>

                      {isEntityLoading ? (
                          <div className="bg-gray-50 rounded-lg border border-gray-200 p-6 text-center">Loading...</div>
                      ) : selectedEntityData ? (
                          <div className="bg-gray-50 rounded-lg border border-gray-200 p-6">
                            <div className="flex justify-between items-center mb-4">
                              <div className="flex items-center space-x-4">
                                <div className="h-12 w-12 rounded-full bg-cyan-100 flex items-center justify-center">
                                  <Database className="h-6 w-6 text-cyan-600" />
                                </div>
                                <div>
                                  <h3 className="text-lg font-medium text-gray-900">{selectedEntity}</h3>
                                  <p className="text-sm text-gray-500">ID: {selectedEntityData.id}</p>
                                </div>
                              </div>
                              <Badge variant="outline" className="bg-green-50 text-green-600 border-green-200">
                                Active
                              </Badge>
                            </div>

                            <Separator className="my-4 bg-gray-200" />

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                              <div>
                                <h4 className="text-sm font-medium text-gray-600 mb-2">Entity Details</h4>
                                <div className="space-y-3">
                                  {/* Display all entity attributes */}
                                  {selectedEntity && entities.find(e => e.name === selectedEntity)?.attributes.map((attr) => (
                                    <div key={attr.name} className="flex justify-between">
                                      <span className="text-sm text-gray-500 capitalize">{attr.name}:</span>
                                      <span className="text-sm text-gray-900">
                                        {selectedEntityData[attr.name] !== undefined && selectedEntityData[attr.name] !== null
                                          ? String(selectedEntityData[attr.name])
                                          : `No ${attr.name}`}
                                      </span>
                                    </div>
                                  ))}
                                </div>
                              </div>

                              <div>
                                <h4 className="text-sm font-medium text-gray-600 mb-2">System Information</h4>
                                <div className="space-y-3">
                                  <div className="flex justify-between">
                                    <span className="text-sm text-gray-500">ID:</span>
                                    <span className="text-sm text-gray-900">{selectedEntityData.id}</span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-sm text-gray-500">Created:</span>
                                    <span className="text-sm text-gray-900">{selectedEntityData.createdAt || 'N/A'}</span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-sm text-gray-500">Last Updated:</span>
                                    <span className="text-sm text-gray-900">{selectedEntityData.updatedAt || 'N/A'}</span>
                                  </div>
                                </div>
                              </div>
                            </div>

                            <div className="mt-6 flex justify-end space-x-2">
                              <Button
                                  variant="outline"
                                  className="border-gray-300 text-gray-600"
                                  onClick={() => setSelectedTab("list")}
                              >
                                Back to List
                              </Button>
                              <Button
                                  variant="outline"
                                  className="border-gray-300 text-cyan-600"
                                  onClick={() => setSelectedTab("update")}
                              >
                                <Edit className="mr-2 h-4 w-4" />
                                Edit
                              </Button>
                            </div>
                          </div>
                      ) : (
                          <div className="bg-gray-50 rounded-lg border border-gray-200 p-6 text-center text-gray-500">
                            No entity selected
                          </div>
                      )}
                    </TabsContent>

                    <TabsContent value="update" className="mt-0">
                      <div className="text-sm text-gray-600 mb-4">
                        Endpoint:{" "}
                        <span className="text-cyan-600 font-mono">
                      PUT {getEntityEndpoint()}/{selectedEntityData?.id}
                    </span>
                      </div>

                      {selectedEntityData ? (
                          <div className="bg-gray-50 rounded-lg border border-gray-200 p-6">
                            <div className="space-y-4">
                              <div className="space-y-2">
                                <Label htmlFor="update-id">ID</Label>
                                <Input
                                    id="update-id"
                                    value={selectedEntityData.id}
                                    className="bg-gray-100 border-gray-300"
                                    disabled
                                />
                              </div>

                              {/* Dynamically generate form fields based on entity attributes */}
                              {selectedEntity && entities.find(e => e.name === selectedEntity)?.attributes.map((attr) => (
                                <div key={attr.name} className="space-y-2">
                                  <Label htmlFor={`update-${attr.name}`}>{attr.name.charAt(0).toUpperCase() + attr.name.slice(1)} {attr.required && '*'}</Label>
                                  {attr.type === 'text' || attr.type === 'textarea' ? (
                                    <Textarea
                                      id={`update-${attr.name}`}
                                      placeholder={`Enter ${attr.name}`}
                                      className="bg-white border-gray-300 min-h-[100px]"
                                      value={updateForm[attr.name] || ''}
                                      onChange={(e) => setUpdateForm({ ...updateForm, [attr.name]: e.target.value })}
                                    />
                                  ) : (
                                    <Input
                                      id={`update-${attr.name}`}
                                      placeholder={`Enter ${attr.name}`}
                                      className="bg-white border-gray-300"
                                      value={updateForm[attr.name] || ''}
                                      onChange={(e) => setUpdateForm({ ...updateForm, [attr.name]: e.target.value })}
                                      type={attr.type === 'number' ? 'number' : 'text'}
                                    />
                                  )}
                                </div>
                              ))}
                            </div>

                            <div className="mt-6 flex justify-end space-x-2">
                              <Button
                                  variant="outline"
                                  className="border-gray-300 text-gray-600"
                                  onClick={() => setSelectedTab("list")}
                              >
                                Cancel
                              </Button>
                              <Button
                                  className="bg-gradient-to-r from-cyan-500 to-blue-600"
                                  onClick={updateEntity}
                                  disabled={isEntityLoading}
                              >
                                <Save className="mr-2 h-4 w-4" />
                                {isEntityLoading ? "Updating..." : `Update ${selectedEntity}`}
                              </Button>
                            </div>
                          </div>
                      ) : (
                          <div className="bg-gray-50 rounded-lg border border-gray-200 p-6 text-center text-gray-500">
                            Please select an entity from the list to update
                          </div>
                      )}
                    </TabsContent>

                    <TabsContent value="delete" className="mt-0">
                      <div className="text-sm text-gray-600 mb-4">
                        Endpoint:{" "}
                        <span className="text-cyan-600 font-mono">
                      DELETE {getEntityEndpoint()}/{selectedEntityData?.id}
                    </span>
                      </div>

                      {selectedEntityData ? (
                          <div className="bg-red-50 rounded-lg border border-red-200 p-6">
                            <div className="flex items-center space-x-4 text-red-600 mb-4">
                              <div className="h-10 w-10 rounded-full bg-red-100 flex items-center justify-center">
                                <Trash2 className="h-5 w-5" />
                              </div>
                              <div>
                                <h3 className="text-lg font-medium text-red-700">Confirm Deletion</h3>
                                <p className="text-sm text-red-600">This action cannot be undone. Please confirm.</p>
                              </div>
                            </div>

                            <div className="bg-white rounded-lg border border-gray-200 p-4 mb-6">
                              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label className="text-gray-600">ID</Label>
                                  <div className="bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-gray-900">
                                    {selectedEntityData.id}
                                  </div>
                                </div>
                                <div className="space-y-2">
                                  <Label className="text-gray-600">Name</Label>
                                  <div className="bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-gray-900">
                                    {selectedEntityData.name}
                                  </div>
                                </div>
                              </div>
                            </div>

                            <div className="bg-red-100 rounded-lg p-4 mb-6 border border-red-200">
                              <p className="text-sm text-red-700">
                                Deleting this {selectedEntity?.toLowerCase()} will remove all associated data and cannot be
                                recovered. Related entities may also be affected.
                              </p>
                            </div>

                            <div className="flex justify-end space-x-2">
                              <Button
                                  variant="outline"
                                  className="border-gray-300 text-gray-600"
                                  onClick={() => setSelectedTab("list")}
                              >
                                Cancel
                              </Button>
                              <Button
                                  variant="destructive"
                                  className="bg-red-600 hover:bg-red-700"
                                  onClick={() => deleteEntity(selectedEntityData.id!)}
                                  disabled={isEntityLoading}
                              >
                                <Trash2 className="mr-2 h-4 w-4" />
                                {isEntityLoading ? "Deleting..." : "Delete Permanently"}
                              </Button>
                            </div>
                          </div>
                      ) : (
                          <div className="bg-gray-50 rounded-lg border border-gray-200 p-6 text-center text-gray-500">
                            Please select an entity from the list to delete
                          </div>
                      )}
                    </TabsContent>

                    <TabsContent value="custom" className="mt-0">
                      <div className="text-sm text-gray-600 mb-4">Custom endpoints for {selectedEntity}</div>

                      <div className="space-y-4">
                        {entities
                            .find((e) => e.name === selectedEntity)
                            ?.endpoints.filter((e) => e.includes("/"))
                            .map((endpoint, index) => (
                                <div
                                    key={index}
                                    className="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm"
                                >
                                  <div className="border-b border-gray-200 bg-gray-50 p-4 flex justify-between items-center">
                                    <div className="flex items-center space-x-3">
                                      <Badge
                                          className={
                                            endpoint.startsWith("GET")
                                                ? "bg-blue-100 text-blue-600 border-blue-200"
                                                : "bg-green-100 text-green-600 border-green-200"
                                          }
                                      >
                                        {endpoint.split(" ")[0]}
                                      </Badge>
                                      <span className="text-gray-900 font-mono text-sm">
                                {getEntityEndpoint()}
                                        {endpoint.split(" ")[1]}
                              </span>
                                    </div>
                                  </div>
                                  <div className="p-4">
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                      <div className="space-y-3">
                                        <h4 className="text-sm font-medium text-gray-600">Description</h4>
                                        <p className="text-sm text-gray-900">
                                          {endpoint.includes("search") && "Search for specific entities based on criteria"}
                                          {endpoint.includes("featured") && "Get featured or highlighted entities"}
                                          {endpoint.includes("process") && "Process or execute an action on the entity"}
                                          {endpoint.includes("history") && "Get historical data or activity log"}
                                          {endpoint.includes("verify") && "Verify or validate the entity"}
                                          {endpoint.includes("refund") && "Process a refund or reversal"}
                                          {endpoint.includes("discount") && "Apply a discount or special pricing"}
                                          {endpoint.includes("low-stock") && "Get items with low inventory levels"}
                                        </p>

                                        <h4 className="text-sm font-medium text-gray-600 mt-4">Parameters</h4>
                                        <div className="bg-gray-50 rounded-md border border-gray-300 p-3">
                                          <code className="text-xs text-gray-700 font-mono">
                                            {endpoint.includes("search") && "?query=string&limit=10&offset=0"}
                                            {endpoint.includes("featured") && "?limit=5&category=string"}
                                            {endpoint.includes("process") && '{ "status": "processing" }'}
                                            {endpoint.includes("history") && "?from=date&to=date"}
                                            {endpoint.includes("verify") && "?token=string"}
                                            {endpoint.includes("refund") && '{ "amount": number, "reason": "string" }'}
                                            {endpoint.includes("discount") && '{ "percentage": number, "code": "string" }'}
                                            {endpoint.includes("low-stock") && "?threshold=10"}
                                          </code>
                                        </div>
                                      </div>

                                      <div className="space-y-3">
                                        <h4 className="text-sm font-medium text-gray-600">Response</h4>
                                        <div className="bg-gray-50 rounded-md border border-gray-300 p-3 h-[200px] overflow-auto">
                                  <pre className="text-xs text-gray-700 font-mono">
                                    {endpoint.includes("GET")
                                        ? JSON.stringify(
                                            {
                                              data: [
                                                {
                                                  id: 1,
                                                  name: `${selectedEntity} 1`,
                                                },
                                                {
                                                  id: 2,
                                                  name: `${selectedEntity} 2`,
                                                },
                                              ],
                                              meta: {
                                                total: 24,
                                                page: 1,
                                                limit: 10,
                                              },
                                            },
                                            null,
                                            2,
                                        )
                                        : JSON.stringify(
                                            {
                                              success: true,
                                              message: "Operation completed successfully",
                                              data: {
                                                id: 1,
                                                name: `${selectedEntity} 1`,
                                              },
                                            },
                                            null,
                                            2,
                                        )}
                                  </pre>
                                        </div>

                                        <div className="flex justify-end mt-4">
                                          <Button className="bg-gradient-to-r from-cyan-500 to-blue-600">Test Endpoint</Button>
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                            ))}
                      </div>
                    </TabsContent>
                  </Tabs>
                </CardContent>
              </Card>
          ) : (
              <Card className="bg-white border-gray-200 backdrop-blur-sm shadow-sm">
                <CardContent className="p-12 flex flex-col items-center justify-center text-center">
                  <div className="h-24 w-24 rounded-full bg-gray-100 flex items-center justify-center mb-6">
                    <Database className="h-12 w-12 text-cyan-500" />
                  </div>
                  <h2 className="text-2xl font-medium text-gray-900 mb-2">Select an Entity</h2>
                  <p className="text-gray-600 max-w-md mb-6">
                    Choose an entity from the sidebar to view and manage its properties, endpoints, and data.
                  </p>
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-3 max-w-2xl">
                    {entities.slice(0, 6).map((entity) => (
                        <Button
                            key={entity.name}
                            variant="outline"
                            className="border-gray-300 text-gray-600 hover:border-cyan-500 hover:text-cyan-600 hover:bg-cyan-50 h-auto py-4 px-6 flex-col"
                            onClick={() => {
                              setSelectedEntity(entity.name)
                              setSelectedTab("list")
                            }}
                        >
                          <Database className="h-5 w-5 mb-2" />
                          <span>{entity.name}</span>
                        </Button>
                    ))}
                  </div>
                </CardContent>
              </Card>
          )}
        </div>
      </div>
  )

  // Render the current page
  return (
      <div className="min-h-screen">
        <div className="relative bg-white min-h-screen">
          {/* Particle canvas */}
          <canvas
              suppressHydrationWarning
              ref={canvasRef}
              className="absolute inset-0 w-full h-full pointer-events-none"
          />

          {/* Header */}
          <header className="relative z-10 border-b border-gray-200 bg-white/80 backdrop-blur-sm">
            <div className="container mx-auto px-4 py-3 flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <Code className="h-6 w-6 text-cyan-500" />
                <span className="text-xl font-bold bg-gradient-to-r from-cyan-500 to-blue-600 bg-clip-text text-transparent">
                CODE FORGE
              </span>
              </div>

              <div className="flex items-center space-x-4">
                {mounted && (
                    <div className="text-sm text-gray-600">
                      {mounted && formatDate(currentTime)} {mounted && formatTime(currentTime)}
                    </div>
                )}
              </div>
            </div>
          </header>

          {/* Main content */}
          <main className="relative z-10 container mx-auto px-4 py-8">
            {currentPage === "home" && renderHomePage()}
            {currentPage === "login" && renderLoginPage()}
            {currentPage === "register" && renderRegisterPage()}
            {currentPage === "dashboard" && renderDashboard()}
          </main>
        </div>
      </div>
  )
}

// Feature card component for the home page
// @ts-ignore
function FeatureCard({ icon: Icon, title, description }) {
  return (
      <div className="bg-white border border-gray-200 backdrop-blur-sm rounded-lg p-6 flex flex-col items-center text-center shadow-sm">
        <div className="h-12 w-12 rounded-full bg-cyan-100 flex items-center justify-center mb-4">
          <Icon className="h-6 w-6 text-cyan-600" />
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">{title}</h3>
        <p className="text-sm text-gray-600">{description}</p>
      </div>
  )
}
