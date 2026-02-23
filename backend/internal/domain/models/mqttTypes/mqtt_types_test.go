package mqttTypes

import "testing"

func TestNewDeviceConfig_Defaults(t *testing.T) {
	c := NewDeviceConfig()
	if c.SamplingNoise != -1 {
		t.Fatalf("expected SamplingNoise -1, got %d", c.SamplingNoise)
	}
	if c.SamplingTemp != -1 {
		t.Fatalf("expected SamplingTemp -1, got %d", c.SamplingTemp)
	}
	if c.Restart != false {
		t.Fatalf("expected Restart false")
	}
	if c.Health != false {
		t.Fatalf("expected Health false")
	}
	if c.Frequency != -1 {
		t.Fatalf("expected Frequency -1, got %d", c.Frequency)
	}
	if c.Delete != false {
		t.Fatalf("expected Delete false")
	}
}
