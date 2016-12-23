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
            Icon = notConnectedIcon = (System.Drawing.Icon)resources.GetObject("NotConnectedIcon");
            this.connectedIcon = (System.Drawing.Icon)resources.GetObject("ConnectedIcon");
            checkingConnection = new Thread(new ThreadStart(CheckingConnectionFun));
            notifyIcon.ContextMenu = GetMenuForIcon();    
            notifyIcon.Visible = true;
            notifyIcon.Icon = Icon;
            checkingConnection.Start();
        }
        private void CheckingConnectionFun() {
            Action act = () => notifyIcon.Icon = Icon = bt.GetConnectedStatus() ? connectedIcon : notConnectedIcon;
            while (true)
            {
                
                Invoke(act);
                Thread.Sleep(1000);
            }
        }
        private ContextMenu GetMenuForIcon()
        {
            ContextMenu menu = new ContextMenu();
            MenuItem exit = new MenuItem("Exit");
            menu.MenuItems.Add(exit);
            exit.Click += Exit_Click;
            return menu;
        }

        private void Exit_Click(object sender, EventArgs e)
        {
            this.Close();
        }
    }
}
