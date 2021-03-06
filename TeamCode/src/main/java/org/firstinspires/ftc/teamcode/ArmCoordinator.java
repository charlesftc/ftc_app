package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.Locale;

import static java.lang.Double.NaN;

public class ArmCoordinator {
    private LinearOpMode opmode;
    private Gamepad gamepad;
    private Shoulder shoulder;
    private Slide slide;
    private ServoElbow elbow;

    private double foldPos[] = {88.3, 0, 10};
    private double preScoringPos1[] = {NaN, 0.1, 60};
    private double scoringPos1[] = {9, 0.91, 45};
    private double intakePos1[] = {-117, 0.67, 110};
    private double drivingPos[] = {-25, 0, 10};
    private double unfoldPos[] = {};
    private double preSamplePos[] = {-60, 0, 10};
    private double samplePos[] = {-124, 0, 96};
    private double samplePos2[] = {-124, 0.8, 96};
    private double verticalPos[] = {0, 0, 120};

    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevUp = false;
    private boolean prevDown = false;
    private boolean prevLeft = false;
    private boolean prevRight = false;

    public ArmCoordinator(LinearOpMode opmode, Gamepad gamepad, final Shoulder shoulder1, final Slide slide1, ServoElbow elbow1) {
        this.opmode = opmode;
        this.gamepad = gamepad;
        this.shoulder = shoulder1;
        this.elbow = elbow1;
        this.slide = slide1;

        opmode.telemetry.addLine().addData("Joints", new Func<String>() {
            @Override
            public String value() {
                return String.format(Locale.getDefault(), "shoulder %.2f, slide %.2f, elbow %.2f",
                        shoulder.getAngle(), slide.getExtension(), elbow.getAngle());
            }
        });

        opmode.telemetry.addLine().addData("Busy", new Func<String>() {
            @Override
            public String value() {
                return String.format(Locale.getDefault(), "S: %b, S: %b, E: %b",
                        shoulder.isBusy(), slide.isBusy(), elbow.isBusy());
            }
        });
    }

    public void update() {
        if (!gamepad.start) {
            if (prevX && !gamepad.x) {
                setPreScoringPos1();
            }

            if (prevY && !gamepad.y) {
                setScoringPos1();
            }

            if (prevA && !gamepad.a) {
                setDrivingPos();
            }

            if (prevDown && !gamepad.dpad_down) {
                setIntakePos1();
            }

            if (prevUp && !gamepad.dpad_up) {
                setFoldPos();
            }

            prevX = gamepad.x;
            prevY = gamepad.y;
            prevA = gamepad.a;
            prevDown = gamepad.dpad_down;
            prevUp = gamepad.dpad_up;
        }

        opmode.telemetry.addData("isBusy()", "%b", isBusy());
        opmode.telemetry.update();
    }

    public void setPreScoringPos1() {
        setGoals(preScoringPos1);
    }

    public void setScoringPos1() {
        setGoals(scoringPos1);
    }

    public void setDrivingPos() {
        setGoals(drivingPos);
    }

    public void setPreSamplePos() {
        setGoals(preSamplePos);
    }

    public void setSamplePos() {
        setGoals(samplePos);
    }

    public void setSamplePos2() {
        setGoals(samplePos2);
    }

    public void setVerticalPos() {
        setGoals(verticalPos);
    }

    public void setFoldPos() {
        setGoals(foldPos);
    }

    public void setIntakePos1() {
        setGoals(intakePos1);
    }

    private void setGoals(double goals[]) {
        shoulder.setGoal(goals[0]);
        slide.setGoal(goals[1]);
        elbow.setGoal(goals[2]);
    }

    public boolean isBusy() {
        return shoulder.isBusy() || slide.isBusy() || elbow.isBusy();
    }

    public void waitTillDone(double timeOut) {
        double startTime = opmode.getRuntime();

        while (isBusy()) {
            opmode.sleep(10);

            shoulder.control(0);
            slide.control(0);
            elbow.control(0);

            if (!opmode.opModeIsActive()) {
                break;
            }

            if (opmode.getRuntime() - startTime > timeOut) {
                break;
            }
        }

        elbow.stop();
    }

    public void asyncMove(double elbowPwr) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                waitTillDone(10);
                elbow.setMaxPosPower(0.7);
            }
        };

        elbow.setMaxPosPower(elbowPwr);
        Thread thread = new Thread(r);
        thread.start();
    }
}
