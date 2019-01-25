package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import static java.lang.Double.NaN;

public class ShoulderPWMControl extends Thread {
    private boolean run = true;

    private DcMotorEx motor;
    private ElapsedTime runtime;

    private double maxInterval = 100;
    private double pulseDuration = 1;
    private double prevPulseStamp = 0;

    private double maxPower = 0.65;
    private double minPower = 0.15;
    private double power = 0.1;

    private double commandVel = NaN;

    public ShoulderPWMControl(DcMotorEx motor) {
        this.motor = motor;
    }

    public void run() {
        runtime = new ElapsedTime();

        while (run) {
            if (Double.isNaN(commandVel) || commandVel == 0) {
                continue;
            }

            double interval = Math.abs(commandVel) * maxInterval;

            if (runtime.milliseconds() - prevPulseStamp > interval) {
                prevPulseStamp = runtime.milliseconds();
                motor.setPower(0);
                busySleep(pulseDuration);
            }

            motor.setPower(power * Math.copySign(1.0, commandVel));
        }
    }

    public void end() {
        run = false;
    }

    public void setCommandVel(double vel) {
        if (vel == 0) {
            motor.setPower(0);
        }

        double pwr = 0;

        commandVel = vel;

        if (!Double.isNaN(vel)) {
            pwr = Range.scale(Math.abs(commandVel), 0, 1, minPower, maxPower);
            setPower(pwr);
        }
    }

    private void busySleep(double millis) {
        double start = runtime.milliseconds();
        while (runtime.milliseconds() - start < millis) {}
    }

    private void setPower(double pwr) {
        power = pwr;
    }

    public double getPower () {
        return power;
    }
}