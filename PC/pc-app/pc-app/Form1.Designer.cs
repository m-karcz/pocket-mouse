using System;
using System.Collections;

namespace pc_app
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            bluetooth.Close();
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            this.bluetooth = new System.IO.Ports.SerialPort(this.components);
            this.button1 = new System.Windows.Forms.Button();
            this.comboBox1 = new System.Windows.Forms.ComboBox();
            this.SensitivityBar = new System.Windows.Forms.TrackBar();
            this.label1 = new System.Windows.Forms.Label();
            this.notifyIcon1 = new System.Windows.Forms.NotifyIcon(this.components);
            ((System.ComponentModel.ISupportInitialize)(this.SensitivityBar)).BeginInit();
            this.SuspendLayout();
            // 
            // bluetooth
            // 
            this.bluetooth.BaudRate = 115200;
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(37, 32);
            this.button1.Margin = new System.Windows.Forms.Padding(3, 2, 3, 2);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(84, 26);
            this.button1.TabIndex = 1;
            this.button1.Text = "Connect";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // comboBox1
            // 
            this.comboBox1.FormattingEnabled = true;
            this.comboBox1.Location = new System.Drawing.Point(190, 32);
            this.comboBox1.Margin = new System.Windows.Forms.Padding(3, 2, 3, 2);
            this.comboBox1.Name = "comboBox1";
            this.comboBox1.Size = new System.Drawing.Size(121, 24);
            this.comboBox1.TabIndex = 2;
            // 
            // SensitivityBar
            // 
            this.SensitivityBar.Location = new System.Drawing.Point(37, 126);
            this.SensitivityBar.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.SensitivityBar.Maximum = 150;
            this.SensitivityBar.Minimum = 50;
            this.SensitivityBar.Name = "SensitivityBar";
            this.SensitivityBar.Size = new System.Drawing.Size(261, 56);
            this.SensitivityBar.TabIndex = 3;
            this.SensitivityBar.Value = 100;
            this.SensitivityBar.ValueChanged += new System.EventHandler(this.SensitivityBar_ValueChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(128, 106);
            this.label1.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(71, 17);
            this.label1.TabIndex = 4;
            this.label1.Text = "Sensitivity";
            // 
            // notifyIcon1
            // 
            this.notifyIcon1.Icon = ((System.Drawing.Icon)(resources.GetObject("notifyIcon1.Icon")));
            this.notifyIcon1.Text = "notifyIcon1";
            this.notifyIcon1.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler(this.notifyIcon1_MouseDoubleClick);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(341, 196);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.SensitivityBar);
            this.Controls.Add(this.comboBox1);
            this.Controls.Add(this.button1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.MaximizeBox = false;
            this.Name = "Form1";
            this.Text = "PicoMouse";
            this.Resize += new System.EventHandler(this.Form1_Resize);
            ((System.ComponentModel.ISupportInitialize)(this.SensitivityBar)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }


        private void Bluetooth_DataReceived(object sender, System.IO.Ports.SerialDataReceivedEventArgs e)
        {
            if(bluetooth.BytesToRead==3)
            {
                int dx = bluetooth.ReadByte();
                int dy = bluetooth.ReadByte();
                int click = bluetooth.ReadByte();
                dx = (sbyte)dx;
                dy = (sbyte)dy;
                BitArray myBA = new BitArray(BitConverter.GetBytes((sbyte)click));
                bluetooth.DiscardInBuffer();
                var actualDx = System.Windows.Forms.Cursor.Position.X;
                var actualDy = System.Windows.Forms.Cursor.Position.Y;
                Form1.SetCursorPos(Sensitivity * (int)dx / 100 + actualDx, Sensitivity * (int)dy / 100 + actualDy);
                if (myBA.Get(0)==true && myBA.Get(1)==false)
                {
                    Form1.MouseLeftClick(true);
                }
                else
                {
                    Form1.MouseLeftClick(false);
                } 
                if (myBA.Get(0) == false && myBA.Get(1) == true)
                {
                    if (!rightClicked)
                    {
                       Form1.MouseRightClick();
                        rightClicked = true;
                    }
                }
                else
                {
                    rightClicked = false;
                }
            }
            else if(bluetooth.BytesToRead == 0)
            {
                System.Diagnostics.Debug.WriteLine("dupa -złe dane");
            }
            else // if BytesToRead==1 or ==2 or >3
            {
                bluetooth.DiscardInBuffer();
            }
            
            
        }

        #endregion
        private System.IO.Ports.SerialPort bluetooth;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.ComboBox comboBox1;
        private System.Windows.Forms.TrackBar SensitivityBar;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.NotifyIcon notifyIcon1;

        public int Sensitivity { get; private set; }
    }
}

