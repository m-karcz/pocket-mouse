using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Runtime.InteropServices;

namespace pc_app
{
    public partial class Form1 : Form
    {
        

        public Form1()
        {
            InitializeComponent();
            this.comboBox1.Items.AddRange(System.IO.Ports.SerialPort.GetPortNames());
            this.bluetooth.DataReceived += Bluetooth_DataReceived;
            this.comboBox1.SelectedIndexChanged += comboBox1_SelectedIndexChanged;
            this.Cursor = new System.Windows.Forms.Cursor(Cursor.Handle);
        }
    
        private void comboBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            bluetooth.PortName = comboBox1.SelectedItem.ToString();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if(!bluetooth.IsOpen && System.IO.Ports.SerialPort.GetPortNames().Contains(bluetooth.PortName))
            {
                bluetooth.Open();
            }
        }
        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        public static extern void mouse_event(uint dwFlags, uint dx, uint dy, uint cButtons, uint dwExtraInfo);

        private const int MOUSEEVENTF_LEFTDOWN = 0x02;
        private const int MOUSEEVENTF_LEFTUP = 0x04;
        private const int MOUSEEVENTF_RIGHTDOWN = 0x08;
        private const int MOUSEEVENTF_RIGHTUP = 0x10;

        public static void MouseLeftClick()
        {
            mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
        }
        public static void MouseRightClick()
        {
            mouse_event(MOUSEEVENTF_RIGHTDOWN | MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0);
        }
        public static void MouseMoveXY(uint x, uint y) {
            mouse_event(0, x, y, 0, 0);
        }
            [DllImport("User32.Dll")]
    public static extern long SetCursorPos(int x, int y);
    }
}
