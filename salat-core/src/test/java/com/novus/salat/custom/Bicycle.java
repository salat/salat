package com.novus.salat.custom;

/**
 * I had forgot about how to do all of this, but fortunately the IDE auto-generates nearly everything.
 */
public class Bicycle {

    int cadence = 0;
    int speed = 0;
    int gear = 1;

    public Bicycle(int cadence, int speed, int gear) {
        this.cadence = cadence;
        this.speed = speed;
        this.gear = gear;
    }

    public int getCadence() {
        return cadence;
    }

    public void setCadence(int cadence) {
        this.cadence = cadence;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getGear() {
        return gear;
    }

    public void setGear(int gear) {
        this.gear = gear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bicycle bicycle = (Bicycle) o;

        if (cadence != bicycle.cadence) return false;
        if (gear != bicycle.gear) return false;
        if (speed != bicycle.speed) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cadence;
        result = 31 * result + speed;
        result = 31 * result + gear;
        return result;
    }

    @Override
    public String toString() {
        return "Bicycle{" +
                "cadence=" + cadence +
                ", speed=" + speed +
                ", gear=" + gear +
                '}';
    }
}
