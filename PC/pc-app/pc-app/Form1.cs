using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Windows.Forms;

namespace pc_app
{
    public partial class Form1 : Form
    {
        System.Drawing.Icon notConnectedIcon;
        System.Drawing.Icon connectedIcon;
        BluetoothMouse bt = new BluetoothMouse();
        Thread checkingConnection;
        public Form1()
        {
            InitializeComponent();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            notConnectedIcon = pc_app.Properties.Resources.NotConnectedIcon;
            Icon = notConnectedIcon;
            connectedIcon = pc_app.Properties.Resources.ConnectedIcon;
            notifyIcon.ContextMenu = GetMenuForIcon();
            notifyIcon.Visible = true;
            notifyIcon.Icon = Icon;
            this.Resize += Form1_Resize;
            this.notifyIcon.DoubleClick += NotifyIcon_DoubleClick;
            this.ShowInTaskbar = false;
            this.WindowState = FormWindowState.Minimized;
            this.trackBar1.ValueChanged += TrackBar1_ValueChanged;
            checkingConnection = new Thread(new ThreadStart(CheckingConnectionFun));
            checkingConnection.Start();
        }

        private void TrackBar1_ValueChanged(object sender, EventArgs e)
        {
            bt.SetSensitivity(trackBar1.Value*25);
        }

        private void NotifyIcon_DoubleClick(object sender, EventArgs e)
        {
            this.WindowState = FormWindowState.Normal;
            this.Show();
        }

        private void Form1_Resize(object sender, EventArgs e)
        {
            if (FormWindowState.Minimized == this.WindowState)
            {
                this.Hide();
            }
        }

        private void CheckingConnectionFun()
        {
            Action act = () => notifyIcon.Icon = Icon = bt.GetConnectedStatus() ? connectedIcon : notConnectedIcon;
            while (true)
            {
                try
                {
                    Invoke(act);
                }
                catch (System.InvalidOperationException e)
                {
                    System.Diagnostics.Debug.WriteLine(e.Message);
                }
                Thread.Sleep(1000);
            }
        }
        private ContextMenu GetMenuForIcon()
        {
            ContextMenu menu = new ContextMenu();
            MenuItem title = new MenuItem("PicoMouse");
            title.Enabled = false;
            menu.MenuItems.Add(title);
            menu.MenuItems.Add("-");
            menu.MenuItems.Add("Sensitivity", NotifyIcon_DoubleClick);
            menu.MenuItems.Add("Exit", Exit_Click);
            return menu;
        }

        private void Exit_Click(object sender, EventArgs e)
        {
            this.Close();
        }
    }
}
