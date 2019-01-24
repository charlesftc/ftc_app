package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.Locale;

import static java.lang.Double.NaN;

public class ShoulderPWMControl extends Thread {
    private boolean run = true;

    private DcMotorEx motor;
    private Gamepad gamepad;
    private ElapsedTime runtime;
    private LinearOpMode opmode;

    private double maxInterval = 100;
    private double pulseDuration = 1;
    private double prevPulseStamp = 0;

    private double maxPower = 0.65;
    private double minPower = 0.15;
    private double power = 0.1;

    private double commandVel = NaN;

    Telemetry.Item myStuff;

    public ShoulderPWMControl(LinearOpMode opmode, DcMotorEx motor, Gamepad gamepad) {
        this.motor = motor;
        this.gamepad = gamepad;

        this.opmode = opmode;

        //myStuff = opmode.telemetry.addData("ShoulderPWM", "");
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

    public void setCommandVel(double vel, double angle) {
        if (vel == 0) {
            motor.setPower(0);
        }

        double pwr = 0;

        if (!Double.isNaN(vel)) {
            //commandVel = vel * (1 - Math.sin(Math.toRadians(angle))) + 0.001;
            commandVel = vel;
            pwr = Range.scale(Math.abs(commandVel), 0, 1, minPower, maxPower);
            setPower(pwr);
        } else {
            commandVel = vel;
        }

        //myStuff.setValue(String.format(Locale.getDefault(), "vel %.2f, commandVel %.2f, pwr %.2f", vel, commandVel, pwr));
    }

    private void busySleep(double millis) {
        double start = runtime.milliseconds();
        while (runtime.milliseconds() - start < millis) {}
    }

    public void setPower(double pwr) {
        power = pwr;
    }

    public double getPower () {
        return power;
    }
}