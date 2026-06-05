# Seat Booking System

A distributed seat booking system built with microservices architecture, 
demonstrating real-world patterns like distributed locking, async payment 
processing, outbox pattern, and horizontal scaling.

## Architecture

<img width="1472" height="1602" alt="image" src="https://github.com/user-attachments/assets/2e17a6e6-3e99-46b9-9a75-3afb334787ac" />


## Tech stack
Java 21
Spring Boot 3.2
Redis
MySQL
Docker

## Key design decisions

**Distributed seat locking** — Redis Lua scripts lock multiple seats atomically. 
Either all seats lock or none do, preventing partial locks under concurrency.

**Async payment** — booking-service fires POST /payment/initiate and returns 
PENDING immediately. Payment-service processes in a background thread and 
notifies via webhook. Booking-service thread is free within 200ms instead 
of blocking for 2-5 seconds.

**Optimistic locking** — @Version on Seat and SeatCategory entities prevents 
concurrent DB writes without pessimistic locks. Retry logic handles conflicts 
on seat category count updates
