package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import static java.lang.Double.NaN;

public class ShoulderPWMControl extends Thread {
    private boolean run = true;

    private DcMotor motor;
    private Gamepad gamepad;
    private ElapsedTime runtime;

    private double maxInterval = 50;
    private double pulseDuration = 1;
    private double prevPulseStamp = 0;

    private double power = 0.05;
    private double commandVel = NaN;
    private double offset = 0;
    private int ticksPerRev = 1680;

    public ShoulderPWMControl(DcMotor motor, Gamepad gamepad) {
        this.motor = motor;
        this.gamepad = gamepad;
    }

    public void run() {
        runtime = new ElapsedTime();

        while (run = true) {
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

    public void setCommandVel(double vel, int offset) {
        if (vel == 0) {
            motor.setPower(0);
        }

        this.offset = offset / (ticksPerRev * 2);

        if (!Double.isNaN(vel)) {
            commandVel = vel * offset;
        } else {
            commandVel = vel;
        }
    }

    private void busySleep(double millis) {
        double start = runtime.milliseconds();
        while (runtime.milliseconds() - start < millis) {}
    }
}