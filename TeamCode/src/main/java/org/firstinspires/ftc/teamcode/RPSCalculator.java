package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

public class RPSCalculator extends Thread {
    private boolean run = true;
    private double RPS;
    private int prevTick;
    private DcMotor motor;
    private int ticksPerRev;
    private double waitTime;
    private ElapsedTime runtime;

    public RPSCalculator(DcMotor motor, int ticksPerRev, double waitTime) {
        this.motor = motor;
        this.ticksPerRev = ticksPerRev;
        this.waitTime = waitTime;
    }

    public void run() {
        prevTick = motor.getCurrentPosition();
        runtime = new ElapsedTime();

        while (run = true) {
            int tick = motor.getCurrentPosition();
            int deltaTick = tick - prevTick;
            double ticksPerSecond = deltaTick / waitTime;
            RPS = ticksPerSecond / ticksPerRev;
            //double currentTime = runtime.milliseconds();
            busySleep(waitTime * 1000);
            /*try {
                opmode.sleep((long)waitTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            //currentTime = runtime.milliseconds() - currentTime;
            prevTick = tick;
        }
    }

    public void end() {
        run = false;
    }

    public double getRPS() {
        return RPS;
    }

    private void busySleep(double millis) {
        double start = runtime.milliseconds();
        while (runtime.milliseconds() - start < millis) {}
    }
}
