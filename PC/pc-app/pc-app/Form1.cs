using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

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
    }
}
