FROM golang:1.23-alpine

WORKDIR /app

COPY go.mod ./
COPY go.sum ./

RUN go build -o beeIoT

EXPOSE 8000

CMD ["./beeIoT"]
