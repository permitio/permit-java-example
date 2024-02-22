# Blog Application with Permit.io Authorization

This example project showcases a simple blog application that implements authorization using permit.io.

## Features

- **User Authentication**: Users can log in to the application using their credentials.
- **Blog Management**: Authenticated users can create new blog posts, read existing posts, update their posts, and
  delete them, subject to their authorization level.
- **Authorization with permit.io**: The application uses permit.io to manage user roles and permissions, ensuring that
  access to certain actions (like updating or deleting a blog post) is appropriately restricted.
- **Role-Based Access Control**: Defines different roles for users, such as viewer, editor, and admin, each with varying
  levels of access and permissions.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing
purposes.

### Prerequisites

Before you begin, ensure you have the following installed:

- Java JDK 21
- Gradle
- Docker (optional, for running permit.io locally)

### 1. Setting Up Permit.io

To use permit.io for authorization, you'll need to create a new project in your workspace.
With your new project, copy your API key, as you will need it to configure the application.

### 2. Starting your local PDP container

The PDP (Policy Decision Point) is the component of permit.io that evaluates access control decisions.
You can run a local PDP container using Docker:

```shell
docker run -it -p 7766:7000 --env PDP_DEBUG=True --env PDP_API_KEY=<YOUR_API_KEY> permitio/pdp-v2:latest
```

### 3. Configuring the Application

Navigate to the `src/main/resources` directory and open the `application.yaml` file.
Paste your API key into the `permit.api-key` property.

> You may use the Cloud PDP by setting the `permit.pdp-url` property to https://cloudpdp.api.permit.io, although it is
> not
> recommended as it does not support ABAC policies used by in this project.

### 4. Running the Application

You can run the application using the following Gradle command:

```shell
./gradlew bootRun
```

Then access the application Swagger at http://localhost:8080/swagger-ui/index.html.

## Usage

The application contains a makeshift user authentication. You can register using the `/api/users/signup` with a username
in the request body, then use it in the `Authorization` header with the username as the Bearer token.
For example:
```shell
curl -X POST "http://localhost:8080/api/users/signup" -H "Content-Type: application/json" -d 'myuser'
curl -X GET "http://localhost:8080/api/blogs" -H "Authorization: Bearer myuser"
```
Alternatively, you can use the Swagger UI authorize with the username.

When signing up, the user is synced to your Permit.io project. You can then assign roles and permissions to the user.

### Blog Resource

The application contains a simple blog resource with the actions `create`, `read`, `update`, and `delete`.

#### 1. Reading Blogs
Assign your user with a `viewer` role and permissions to `read` blogs. Then you can list the blogs using:
```shell
curl -X GET "http://localhost:8080/api/blogs" -H "Authorization: Bearer myuser"
```
Or get specific blog by ID:
```shell
curl -X GET "http://localhost:8080/api/blogs/1" -H "Authorization: Bearer myuser"
```

#### 2. Creating Blogs
Assign your user with an `editor` role and permissions to `create` blogs. Then you can create a blog using:
```shell
curl -X POST "http://localhost:8080/api/blogs" -H "Authorization: Bearer myuser" -H "Content-Type: application/json" -d 'This is my blog'
```

#### 3. Modifying Blogs
In order to allow users only to update or delete their own blogs, the application uses ABAC policies.

1. Create a new Resource Set called `Owned Blog` with a condition where the `resource.author` attribute equals (ref) the `user.key` attribute.
2. Assign the `editor` role permissions to `update` and `delete` the `Owned Blog` resource set.

Then you can update or delete your blog, assuming the blog with ID 3 was created on [step 2](#2-creating-blogs):
```shell
curl -X PUT "http://localhost:8080/api/blogs/3" -H "Authorization: Bearer myuser" -H "Content-Type: application/json" -d 'This is my updated blog'
curl -X DELETE "http://localhost:8080/api/blogs/3" -H "Authorization: Bearer myuser"
```
Modifying other blogs will result in a 403 Forbidden response.
```shell
curl -X DELETE "http://localhost:8080/api/blogs/1" -H "Authorization: Bearer myuser"
# 403 Forbidden
```

#### 4. Admin Access
Assign your user with an `admin` role and permissions for the entire Blog resource. Then you can perform any action on the blogs, including deleting other user's blogs.
```shell
curl -X DELETE "http://localhost:8080/api/blogs/1" -H "Authorization: Bearer myuser"
```