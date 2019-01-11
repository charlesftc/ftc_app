package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.ElapsedTime;

public class RPSCalculator extends Thread {
    private boolean run = true;
    private double RPS;
    private int prevTick;
    private DcMotor motor;
    private int ticksPerRev;
    private double waitTime;

    public RPSCalculator(DcMotor motor, int ticksPerRev, double waitTime) {
        this.motor = motor;
        this.ticksPerRev = ticksPerRev;
        this.waitTime = waitTime;
    }

    public void run() {
        prevTick = motor.getCurrentPosition();
        while (run = true) {
            int tick = motor.getCurrentPosition();
            int deltaTick = tick - prevTick;
            double ticksPerSecond = deltaTick / waitTime;
            RPS = ticksPerSecond / ticksPerRev;
            try {
                Thread.sleep((long)waitTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            prevTick = tick;
        }
    }

    public void end() {
        run = false;
    }

    public double getRPS() {
        return RPS;
    }
}
