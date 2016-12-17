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
            this.SuspendLayout();
            // 
            // bluetooth
            // 
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(37, 32);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 1;
            this.button1.Text = "Connect";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // comboBox1
            // 
            this.comboBox1.FormattingEnabled = true;
            this.comboBox1.Location = new System.Drawing.Point(246, 32);
            this.comboBox1.Name = "comboBox1";
            this.comboBox1.Size = new System.Drawing.Size(121, 24);
            this.comboBox1.TabIndex = 2;
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(379, 321);
            this.Controls.Add(this.comboBox1);
            this.Controls.Add(this.button1);
            this.Margin = new System.Windows.Forms.Padding(4);
            this.Name = "Form1";
            this.Text = "Form1";
            this.ResumeLayout(false);

        }


        private void Bluetooth_DataReceived(object sender, System.IO.Ports.SerialDataReceivedEventArgs e)
        {
            if(bluetooth.BytesToRead==3)
            {
                int dx = bluetooth.ReadByte();
                int dy = bluetooth.ReadByte();
                int click = bluetooth.ReadByte();
                //dx = dx > 127 ? -dx : dx;
                dx = (sbyte)dx;
                dy = (sbyte)dy;
                BitArray myBA = new BitArray(BitConverter.GetBytes((sbyte)click));
                bluetooth.DiscardInBuffer();
                /*System.Diagnostics.Debug.WriteLine(dx.ToString() + "," + dy.ToString());
                foreach(bool x in myBA)
                {
                    System.Diagnostics.Debug.Write( x ? "1" : "0");
                }
                System.Diagnostics.Debug.Write('\n');*/
                var actualDx = System.Windows.Forms.Cursor.Position.X;
                var actualDy = System.Windows.Forms.Cursor.Position.Y;
                System.Windows.Forms.Cursor.Position = new System.Drawing.Point(actualDx + dx, actualDy + dy);
                if(myBA.Get(0)==true && myBA.Get(1)==false)
                {
                   // System.Diagnostics.Debug.WriteLine("klik left");
                    Form1.MouseLeftClick();
                }
                else if (myBA.Get(0) == false && myBA.Get(1) == true)
                {
                   // System.Diagnostics.Debug.WriteLine("klik right");
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
    }
}

