# Mini-Amazon / Mini-UPS Project

Welcome to the Mini-Amazon / Mini-UPS Project! This project simulates the interaction between an online store (Amazon) and a shipping company (UPS) within a simulated world. The goal is to create a seamless system that allows users to purchase items and have them delivered efficiently.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
- [Architecture](#architecture)
- [Protocol Specification](#protocol-specification)
- [Product Differentiation](#product-differentiation)
- [Contributing](#contributing)
- [License](#license)

## Introduction

This project involves two main components:
1. **Mini-Amazon**: An online store where users can purchase items.
2. **Mini-UPS**: A shipping service that delivers purchased items.

Both components interact with a simulated world, where warehouses and trucks are controlled to manage inventory and deliveries.

## Features

### Amazon
- **Buy Items**: Users can buy items, which are then delivered to specified addresses.
- **Pack Shipments**: Manage inventory and pack items for delivery.
- **Load Shipments**: Load packed items onto trucks for delivery.
- **Query Status**: Check the status of shipments and items.

### UPS
- **Deliver Shipments**: Manage trucks and deliver items to specified addresses.
- **Pickup Shipments**: Pick up items from warehouses.
- **Query Status**: Check the status of trucks and shipments.

## Setup

### Prerequisites
- Docker
- Docker Compose
- Java (for backend services)
- Hibernate (for database)

### Installation
1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/mini-amazon-ups.git
    cd mini-amazon-ups
    ```

2. Build Docker images:
    ```bash
    docker-compose build
    ```

3. Start the services:
    ```bash
    docker-compose up
    ```

## Usage

### Amazon
1. Access the Amazon web interface at `http://localhost:8080`.
2. Use the interface to buy items, pack shipments, load shipments, and query the status.

### UPS
1. Access the UPS web interface at `http://localhost:8081`.
2. Use the interface to manage deliveries, pickups, and query truck statuses.

## Architecture

The system is designed with the following components:

### Backend
- **Amazon Service**: Handles purchasing, packing, and loading.
- **UPS Service**: Manages deliveries and pickups.

### Database
- **Postgres**: Stores inventory, shipments, and truck statuses.

### Communication
- **Google Protocol Buffers**: Used for communication between services and the simulated world.

### Simulated World
- Interacts with backend services to simulate real-world logistics.

## Protocol Specification

The protocol specification defines how Amazon and UPS communicate with each other and the simulated world. It includes the following commands and responses:

### Amazon Commands
- **buy**: Request to buy items.
- **topack**: Request to pack items for delivery.
- **load**: Request to load items onto trucks.
- **queries**: Query the status of packages.

### Amazon Responses
- **arrived**: Notification that items have arrived.
- **ready**: Notification that items are packed.
- **loaded**: Notification that items are loaded onto trucks.
- **packagestatus**: Status of queried packages.
- **error**: Error notification.

### UPS Commands
- **deliveries**: Request to deliver items.
- **pickups**: Request to pick up items from warehouses.
- **queries**: Query the status of trucks.

### UPS Responses
- **completions**: Notification of completed deliveries or pickups.
- **delivered**: Notification of delivered packages.
- **truckstatus**: Status of queried trucks.
- **error**: Error notification.

## Product Differentiation

To differentiate our product from others, we have implemented the following features:
- **Searchable Product Catalog**: Users can search for products.
- **Order Status Tracking**: Users can track the status of their orders.
- **Customizable Delivery Addresses**: Users can specify delivery addresses.
- **User Accounts**: Users can create accounts to manage their orders and shipments.


This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
