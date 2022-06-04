import { useState, useEffect, useCallback } from "react";
import {
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  AreaChart,
  Area,
} from "recharts";

const API_URL = "http://localhost:3001";

function App() {
  const [records, setRecords] = useState(false);

  const [rangedRecords, setRangedRecords] = useState(false);

  const getRecordsMemoized = useCallback(getRecords, []);

  useEffect(() => {
    const id = setInterval(getRecordsMemoized, 1000);
    return () => clearInterval(id);
  }, [getRecordsMemoized]);

  function getRecords() {
    fetch(API_URL)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        setRecords(data);
        prepareRecordsForCharts(data.slice(-30));
      });
  }

  function prepareRecordsForCharts(data) {
    setRangedRecords(
      data.map((record) => ({
        cpu: record.cpu,
        memory_usage: Math.round(
          (record.memory_used / record.memory_total) * 100
        ),
        disk_usage: record.disk_usage,
      }))
    );
  }

  return (
    <>
      <div style={{ width: "900px" }}>
        <span style={{ display: "block", textAlign: "center" }}>CPU</span>
        <AreaChart width={800} height={300} data={rangedRecords}>
          <defs>
            <linearGradient id="colorCPU" x1="0" y1="0" x2="0" y2="1">
              <stop stopColor="#2196F3" stopOpacity={0.45} />
            </linearGradient>
          </defs>
          <Area
            type="monotone"
            isAnimationActive={false}
            dataKey="cpu"
            stroke="#0D47A1"
            fill="url(#colorCPU)"
          />
          <CartesianGrid stroke="#ccc" />
          <XAxis tick={false} />
          <YAxis type="number" domain={[0, 100]} />
          <Tooltip />
        </AreaChart>
      </div>
      <div style={{ width: "900px" }}>
        <span style={{ display: "block", textAlign: "center" }}>
          Memory{" "}
          {records
            ? `${Math.round(
                records.slice(-1)[0].memory_used / 1024
              )} MB / ${Math.round(
                records.slice(-1)[0].memory_total / 1024
              )} MB`
            : ""}
        </span>
        <AreaChart width={800} height={300} data={rangedRecords}>
          <defs>
            <linearGradient id="colorMemory" x1="0" y1="0" x2="0" y2="1">
              <stop stopColor="#9C27B0" stopOpacity={0.45} />
            </linearGradient>
          </defs>
          <Area
            type="monotone"
            isAnimationActive={false}
            dataKey="memory_usage"
            stroke="#4A148C"
            fill="url(#colorMemory)"
          />
          <CartesianGrid stroke="#ccc" />
          <XAxis tick={false} />
          <YAxis type="number" domain={[0, 100]} />
          <Tooltip />
        </AreaChart>
      </div>
      <div style={{ width: "900px" }}>
        <span style={{ display: "block", textAlign: "center" }}>
          Disk usage
        </span>
        <AreaChart width={800} height={300} data={rangedRecords}>
          <defs>
            <linearGradient id="colorDisk" x1="0" y1="0" x2="0" y2="1">
              <stop stopColor="#84d887" stopOpacity={0.45} />
            </linearGradient>
          </defs>
          <Area
            type="monotone"
            isAnimationActive={false}
            dataKey="disk_usage"
            stroke="green"
            fill="url(#colorDisk)"
          />
          <CartesianGrid stroke="#ccc" />
          <XAxis tick={false} />
          <YAxis type="number" domain={[0, 100]} />
          <Tooltip />
        </AreaChart>
      </div>
    </>
  );
}

export default App;
