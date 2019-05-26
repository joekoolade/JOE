package hello.world;

public class HwThread extends Thread {
    
    public void run()
    {
        while(true)
        {
            System.out.println(this.getClass().getName() + ": Hello World");
            try
            {
                Thread.sleep(1100);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
