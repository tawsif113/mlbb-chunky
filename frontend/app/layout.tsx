import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "MLBB Chunky",
  description: "Know what to pick, why to pick it, and what the community is playing.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
