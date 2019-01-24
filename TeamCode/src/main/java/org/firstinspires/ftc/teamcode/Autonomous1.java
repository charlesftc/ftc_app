package org.firstinspires.ftc.teamcode;

//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/*import org.firstinspires.ftc.teamcode.Shoulder;
import org.firstinspires.ftc.teamcode.ServoElbow;
import org.firstinspires.ftc.teamcode.Slide;
import org.firstinspires.ftc.teamcode.DiffDrive;*/

//@Autonomous(name="Autonomous1 ", group="Linear")
public class Autonomous1 extends LinearOpMode {
    private Shoulder shoulder = new Shoulder(this, gamepad1);
    private ServoElbow servoElbow = new ServoElbow(this, gamepad1);
    private Slide slide = new Slide(this, gamepad1);
    private AutoDrive autoDrive = new AutoDrive(this, gamepad1);

    @Override
    public void runOpMode() {

    }
}
