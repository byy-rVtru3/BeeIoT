FROM golang:1.25-alpine AS builder

WORKDIR /app
COPY . .
RUN go mod download



RUN GOMAXPROCS=1 go build -p 1 -o /beeIoT ./cmd/server/main.go

FROM alpine:3.19
WORKDIR /app
COPY --from=builder /beeIoT /app/beeIoT

EXPOSE 8000
CMD ["/app/beeIoT"]
