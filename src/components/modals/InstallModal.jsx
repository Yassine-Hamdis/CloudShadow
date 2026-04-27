import { useState }    from 'react'
import { Copy, Check, AlertTriangle, X } from 'lucide-react'
import toast            from 'react-hot-toast'

const TABS = ['Normal', 'Docker', 'Kubernetes', 'Env Variables']

const CopyButton = ({ text }) => {
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    await navigator.clipboard.writeText(text)
    setCopied(true)
    toast.success('Copied to clipboard!')
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <button
      onClick={handleCopy}
      className="
        flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs
        border border-[#374151] text-[#9AA6B2]
        hover:border-[#3f51b5] hover:text-[#3f51b5]
        transition-all
      "
    >
      {copied
        ? <><Check className="w-3.5 h-3.5" /> Copied!</>
        : <><Copy className="w-3.5 h-3.5" /> Copy</>
      }
    </button>
  )
}

export default function InstallModal({ server, onClose }) {
  const [activeTab, setActiveTab] = useState(0)
  const inst = server.installInstructions

  const tabContent = [
    inst?.normalServer,
    inst?.docker,
    inst?.kubernetes,
    inst?.envVariables,
  ]

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-[#1f2937] border border-[#374151] rounded-[1.75rem] w-full max-w-xl shadow-2xl max-h-[90vh] flex flex-col app-panel-soft overflow-hidden">

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#374151]/70 flex-shrink-0">
          <div>
            <h2 className="text-base font-semibold text-[#E6EEF2]">
              Install Agent — {server.name}
            </h2>
            <p className="text-xs text-[#9AA6B2] mt-0.5">
              Follow the instructions to connect your server
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-[#9AA6B2] hover:text-[#E6EEF2] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Token warning */}
        <div className="mx-6 mt-4 p-4 rounded-2xl bg-[#FFC107]/10 border border-[#FFC107]/25 flex items-start gap-3 flex-shrink-0">
          <AlertTriangle className="w-4 h-4 text-[#FFC107] flex-shrink-0 mt-0.5" />
          <div>
            <p className="text-xs font-semibold text-[#FFC107]">
              Save this token — it won't be shown again!
            </p>
            <div className="flex items-center gap-2 mt-1">
              <code className="text-xs font-mono text-[#E6EEF2] bg-[#0f1724] px-2 py-0.5 rounded">
                {server.token}
              </code>
              <CopyButton text={server.token} />
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 px-6 pt-4 flex-shrink-0 flex-wrap">
          {TABS.map((tab, idx) => (
            <button
              key={tab}
              onClick={() => setActiveTab(idx)}
              className={`
                app-button-sm px-3 text-xs font-medium transition-all
                ${activeTab === idx
                  ? 'bg-[#3f51b5] text-white'
                  : 'text-[#9AA6B2] hover:text-[#E6EEF2] hover:bg-[#374151]'
                }
              `}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* Tab content */}
        <div className="px-6 pb-6 pt-4 flex-1 overflow-auto">
          {tabContent[activeTab] ? (
            <div className="relative">
              <div className="absolute top-3 right-3">
                <CopyButton text={tabContent[activeTab]} />
              </div>
              <pre className="
                bg-[#0f1724] border border-[#374151] rounded-2xl
                p-4 pr-24 text-xs font-mono text-[#E6EEF2]
                overflow-x-auto whitespace-pre-wrap leading-7
              ">
                {tabContent[activeTab]}
              </pre>
            </div>
          ) : (
            <div className="flex items-center justify-center h-24">
              <p className="text-sm text-[#9AA6B2]">No instructions available</p>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-[#374151]/70 flex justify-end flex-shrink-0">
          <button
            onClick={onClose}
            className="
              app-button-sm px-5 text-sm font-medium
              bg-[#374151] hover:bg-[#4b5563] text-[#E6EEF2]
              transition-all
            "
          >
            Done
          </button>
        </div>
      </div>
    </div>
  )
}