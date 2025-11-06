FROM golang:1.25-alpine AS builder

WORKDIR /app
COPY . .
RUN go mod download



RUN go build -o /beeIoT ./cmd/server

FROM alpine:3.19
WORKDIR /app
COPY --from=builder /beeIoT /app/beeIoT

EXPOSE 8000
CMD ["/app/beeIoT"]
