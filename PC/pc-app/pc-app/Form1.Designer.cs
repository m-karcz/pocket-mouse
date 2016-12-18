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
            this.bluetooth = new System.IO.Ports.SerialPort(this.components);
            this.button1 = new System.Windows.Forms.Button();
            this.comboBox1 = new System.Windows.Forms.ComboBox();
            this.SensitivityBar = new System.Windows.Forms.TrackBar();
            this.label1 = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.SensitivityBar)).BeginInit();
            //Sensitivity = SensitivityBar.Value;
            this.SuspendLayout();
            // 
            // bluetooth
            // 
            this.bluetooth.BaudRate = 115200;
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(28, 26);
            this.button1.Margin = new System.Windows.Forms.Padding(2);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(56, 19);
            this.button1.TabIndex = 1;
            this.button1.Text = "Connect";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // comboBox1
            // 
            this.comboBox1.FormattingEnabled = true;
            this.comboBox1.Location = new System.Drawing.Point(184, 26);
            this.comboBox1.Margin = new System.Windows.Forms.Padding(2);
            this.comboBox1.Name = "comboBox1";
            this.comboBox1.Size = new System.Drawing.Size(92, 21);
            this.comboBox1.TabIndex = 2;
            // 
            // SensitivityBar
            // 
            this.SensitivityBar.Location = new System.Drawing.Point(59, 160);
            this.SensitivityBar.Maximum = 150;
            this.SensitivityBar.Minimum = 50;
            this.SensitivityBar.Name = "SensitivityBar";
            this.SensitivityBar.Size = new System.Drawing.Size(172, 45);
            this.SensitivityBar.TabIndex = 3;
            this.SensitivityBar.Value = 100;
            this.SensitivityBar.ValueChanged += new System.EventHandler(this.SensitivityBar_ValueChanged);
            
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(109, 122);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(54, 13);
            this.label1.TabIndex = 4;
            this.label1.Text = "Sensitivity";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(284, 261);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.SensitivityBar);
            this.Controls.Add(this.comboBox1);
            this.Controls.Add(this.button1);
            this.Name = "Form1";
            this.Text = "Form1";
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
                    Form1.MouseLeftClick();
                }
                else if (myBA.Get(0) == false && myBA.Get(1) == true)
                {
                    Form1.MouseRightClick();
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

        public int Sensitivity { get; private set; }
    }
}

