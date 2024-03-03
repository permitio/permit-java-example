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
- Docker (optional, for running Permit.io PDP locally)

### 1. Setting Up Permit.io using Terraform

Login to your Permit.io account and create a new project in your workspace and copy your API key.
Set the `PERMIT_API_KEY` environment variable to your API key:

```shell
export PERMIT_API_KEY=<YOUR_API_KEY>
```
Then run the following commands to create the resources in your Permit.io project:
```shell
terraform init
terraform plan
terraform apply
```

You should see under the Policy Editor a "Blog" and "Owned Blog" resources, and the roles `viewer`, `editor`, and `admin`.
![image](https://github.com/permitio/permit-java-example/assets/12188774/d1fd9a57-aae0-42c1-a3e5-81e9af912b91)

### 2. Starting your local PDP container

The PDP (Policy Decision Point) is the component of permit.io that evaluates access control decisions.
You can run a local PDP container using Docker:

```shell
docker run -it -p 7766:7000 --env PDP_DEBUG=True --env PDP_API_KEY=<YOUR_API_KEY> permitio/pdp-v2:latest
```

> You may use the Cloud PDP by modifying the `src/resources/application.yaml` config, setting the `permit.pdpUrl` property to https://cloudpdp.api.permit.io. 
> It is not recommended as it does not support ABAC policies used in this project.

### 3. Running the Application

You can run the application using the following Gradle command:

```shell
./gradlew bootRun
```

Then access the application Swagger at http://localhost:8080/swagger-ui/index.html.

## Usage

The application contains a makeshift user authentication. Create a new user using the `/api/users/signup` endpoint:
```shell
curl -X POST "http://localhost:8080/api/users/signup" -H "Content-Type: application/json" -d 'myuser'
```
You should be able to view the user in your Permit.io project, under Directory > All Tenants.

Initially the user has no roles, therefor it cannot do much. For example, trying to list the blogs will result in a 403 Forbidden response:
```shell
curl -X GET "http://localhost:8080/api/blogs" -H "Authorization: Bearer myuser"
# 403 Forbidden
```
Assign the user with a `viewer` role using:
```shell
curl -X POST "http://localhost:8080/api/users/assign-role" -H "Authorization: Bearer myuser" -d "viewer"
```

Alternatively, you can use the Swagger UI authorize with the username.

### Blog Resource

The application contains a simple blog resource with the actions `create`, `read`, `update`, and `delete`.

#### 1. Reading Blogs
Assign your user with a `viewer` role (permissions to `read` blogs). Then you can list the blogs using:
```shell
curl -X GET "http://localhost:8080/api/blogs" -H "Authorization: Bearer myuser"
```
Or get specific blog by ID:
```shell
curl -X GET "http://localhost:8080/api/blogs/1" -H "Authorization: Bearer myuser"
```

#### 2. Creating Blogs
Assign your user with an `editor` role (permissions to `create` blogs). Then you can create a blog using:
```shell
curl -X POST "http://localhost:8080/api/blogs" -H "Authorization: Bearer myuser" -H "Content-Type: application/json" -d 'This is my blog'
```

#### 3. Modifying Blogs
Editors can `update` and `delete` their own blogs, defined by a Resource Set called `Owned Blog` with a condition where the `resource.author` attribute equals (ref) the `user.key` attribute.

You can update or delete your blog, assuming the blog with ID 3 was created on [step 2](#2-creating-blogs):
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
Assign your user with an `admin` role. Then you can perform any action on the blogs, including deleting other user's blogs.
```shell
curl -X DELETE "http://localhost:8080/api/blogs/1" -H "Authorization: Bearer myuser"
```

## Testing

This example project contains integration tests that demonstrate the authorization flow using permit.io. 

You can run all tests using the following Gradle command:
```shell
./gradlew test
```

First, the test suite create a viewer, editor and admin users, and assign them with the respective roles.
Then, it asserts:
* Unauthenticated users or unknown users cannot access the API.
* Viewer can read blogs, but cannot create, update, or delete them. (RBAC)
* Editor can read and create blogs, and can update or delete their own blogs. (ABAC)
* Editor cannot update or delete other user's blogs. 
* Viewer can comment on blogs, and can update or delete their own comments. (ABAC)
* Editor can delete comments on their own blogs. (ReBAC)
* Admin can delete other user's blogs and comments.
