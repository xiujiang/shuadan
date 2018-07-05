import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WinAdapter extends WindowAdapter {
    @Override
    public void windowClosing(WindowEvent e) {
        // TODO Auto-generated method stub
        //System.out.println("Window closing"+e.toString());
        System.out.println("我关了");
        System.exit(0);
    }
    @Override
    public void windowActivated(WindowEvent e) {
        //每次获得焦点 就会触发
        System.out.println("我活了");
        //super.windowActivated(e);
    }
    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
        System.out.println("我开了");
        //super.windowOpened(e);
    }
}
