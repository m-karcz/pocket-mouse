using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Net.Sockets;
using System.Windows.Forms;

using InTheHand.Net;
using InTheHand.Net.Bluetooth;
using InTheHand.Net.Sockets;

namespace pc_app
{
    class BluetoothMouse : IDisposable
    {
        public BluetoothMouse()
        {
            StartListeningForConnection();
        }

        public bool GetConnectedStatus()
        {
            return isConnected;
        }

        private bool isConnected = false;

        private Thread listenForConnection;

        private BluetoothListener listener = new BluetoothListener(new Guid("00001101-0000-1000-8000-00805f9b34fb"));
        private BluetoothClient client;

        private void StartListeningForConnection()
        {
            listenForConnection = new Thread(new ThreadStart(WaitingForConnection));
            listenForConnection.Start();
        }

        private void ReadingData(IAsyncResult result)
        {
            var strm = (NetworkStream)result.AsyncState;
            int X = 0;
            int Y = 0;
            int Rest = 0;

            foreach (var whichByte in new MouseByte[3] { MouseByte.X, MouseByte.Y, MouseByte.Rest })
            {
                switch (whichByte)
                {
                    case MouseByte.X:
                        X = strm.ReadByte();
                        if (-1 == X)
                        {
                            isConnected = false;
                            MRE.Set();
                            return;
                        }
                        break;
                    case MouseByte.Y:
                        Y = strm.ReadByte();
                        break;
                    case MouseByte.Rest:
                        Rest = strm.ReadByte();
                        break;
                }
            }

            var actualPosition = Cursor.Position;
            MouseControl.SetCursorPos(actualPosition.X + (sbyte)X, actualPosition.Y + (sbyte)Y);
            MouseControl.LeftClickAction(Rest == 1);
            System.Diagnostics.Debug.WriteLine("" + X + Y);
            strm.BeginRead(new byte[3], 0, 3, new AsyncCallback(ReadingData), strm);
        }

        ManualResetEvent MRE = new ManualResetEvent(false);

        private void WaitingForConnection()
        {
            listener.Start();
            while (true)
            {
                if (listener.Pending())
                {
                    client = listener.AcceptBluetoothClient();
                    var stream = client.GetStream();

                    stream.BeginRead(new byte[3], 0, 3, new AsyncCallback(ReadingData), stream);
                    isConnected = true;
                    MRE.WaitOne();
                }
                else
                {
                    Thread.Sleep(500);
                }
            }
        }

        #region IDisposable Support
        private bool disposedValue = false; // To detect redundant calls

        protected virtual void Dispose(bool disposing)
        {
            if (!disposedValue)
            {
                if (disposing)
                {
                    // TODO: dispose managed state (managed objects).
                    listenForConnection.Abort();
                }

                // TODO: free unmanaged resources (unmanaged objects) and override a finalizer below.
                // TODO: set large fields to null.

                disposedValue = true;

            }
        }

        // TODO: override a finalizer only if Dispose(bool disposing) above has code to free unmanaged resources.
        // ~Bluetooth() {
        //   // Do not change this code. Put cleanup code in Dispose(bool disposing) above.
        //   Dispose(false);
        // }

        // This code added to correctly implement the disposable pattern.
        public void Dispose()
        {
            // Do not change this code. Put cleanup code in Dispose(bool disposing) above.
            Dispose(true);
            // TODO: uncomment the following line if the finalizer is overridden above.
            // GC.SuppressFinalize(this);
        }
        #endregion

     
    }
}
