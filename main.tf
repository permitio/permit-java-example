terraform {
  required_providers {
    permitio = {
      source  = "permitio/permit-io"
      version = "~> 0.0.1"
    }
  }
}

variable "permit_api_key" {
  type        = string
  description = "The API key for the Permit.io API"
}

provider "permitio" {
  api_url = "https://api.permit.io"
  api_key = var.permit_api_key
}

resource "permitio_resource" "blog" {
  key     = "blog"
  name    = "Blogs"
  actions = {
    "create" = { "name" = "create" }
    "read"   = { "name" = "read" }
    "update" = { "name" = "update" }
    "delete" = { "name" = "delete" }
  }
  attributes = {
    "author" = {
      "description" = "The user key who created the blog"
      "type"        = "string"
    }
  }
}

resource "permitio_resource_set" "own_blog" {
  key        = "own_blog"
  name       = "Owned Blogs"
  resource   = permitio_resource.blog.key
  conditions = jsonencode({
    "allOf" : [
      { "resource.author" : { "equals" : { "ref" : "user.key" } } }
    ]
  })
}

resource "permitio_role" "viewer" {
  key         = "viewer"
  name        = "viewer"
  description = "Read all blogs"
  permissions = ["blog:read"]
  extends     = []
  depends_on  = [
    permitio_resource.blog,
  ]
}

resource "permitio_role" "editor" {
  key         = "editor"
  name        = "editor"
  description = "Create blogs, update and delete them"
  permissions = ["blog:read", "blog:create"] # TODO "own_blog:update", "own_blog:delete"
  extends     = []
  depends_on  = [
    permitio_resource.blog,
    permitio_resource_set.own_blog,
  ]
}

resource "permitio_role" "admin" {
  key         = "admin"
  name        = "admin"
  description = "All permissions on blogs"
  permissions = ["blog:read", "blog:create", "blog:update", "blog:delete"]
  extends     = []
  depends_on  = [
    permitio_resource.blog,
  ]
}
