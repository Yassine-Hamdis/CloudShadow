import { useEffect, useState } from 'react'
import toast                    from 'react-hot-toast'
import { Plus, Trash2, Users }  from 'lucide-react'
import { getUsers, deleteUser } from '../../api/users'
import AddUserModal             from '../../components/modals/AddUserModal'
import LoadingSpinner           from '../../components/common/LoadingSpinner'
import useAuthStore             from '../../store/authStore'

const RoleBadge = ({ role }) => (
  <span
    className={`
      inline-flex items-center px-2.5 py-0.5 rounded-full
      text-xs font-semibold
      ${role === 'ADMIN'
        ? 'bg-[#3f51b5]/20 text-[#3f51b5] border border-[#3f51b5]/30'
        : 'bg-[#374151] text-[#9AA6B2] border border-[#4b5563]'
      }
    `}
  >
    {role}
  </span>
)

export default function UsersPage() {
  const { email: currentEmail } = useAuthStore()

  const [users, setUsers]         = useState([])
  const [loading, setLoading]     = useState(true)
  const [showAdd, setShowAdd]     = useState(false)
  const [deletingId, setDeletingId] = useState(null)

  const loadUsers = async () => {
    setLoading(true)
    try {
      const data = await getUsers()
      setUsers(data)
    } catch {
      toast.error('Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadUsers() }, [])

  const handleCreated = (user) => {
    setUsers((prev) => [...prev, user])
  }

  const handleDelete = async (user) => {
    if (user.email === currentEmail) {
      toast.error("You can't delete your own account")
      return
    }
    if (!confirm(`Delete user "${user.email}"?`)) return

    setDeletingId(user.id)
    try {
      await deleteUser(user.id)
      setUsers((prev) => prev.filter((u) => u.id !== user.id))
      toast.success(`User "${user.email}" deleted`)
    } catch {
      toast.error('Failed to delete user')
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="space-y-6">

      {/* Page header */}
      <div className="app-panel-soft rounded-[1.75rem] p-6 flex items-center justify-between flex-wrap gap-4">
        <div>
          <div className="text-[11px] uppercase tracking-[0.24em] text-[#9AA6B2] mb-2">Access Control</div>
          <h1 className="page-title">Users</h1>
          <p className="page-subtitle">
            Manage team members — {users.length} user{users.length !== 1 ? 's' : ''}
          </p>
        </div>
        <button
          onClick={() => setShowAdd(true)}
          className="
            app-button flex items-center gap-2
            bg-[#3f51b5] hover:bg-[#3949a3] text-white text-sm font-medium
            transition-all shadow-lg shadow-[#3f51b5]/20
          "
        >
          <Plus className="w-4 h-4" />
          Add User
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex items-center justify-center h-48">
          <LoadingSpinner size="lg" />
        </div>
      ) : users.length === 0 ? (
        <div className="
          bg-[#1f2937] border border-dashed border-[#374151] rounded-2xl
          flex flex-col items-center justify-center py-20 text-center app-panel-soft
        ">
          <div className="p-4 rounded-xl bg-[#374151] mb-4">
            <Users className="w-8 h-8 text-[#9AA6B2]" />
          </div>
          <h3 className="text-base font-semibold text-[#E6EEF2] mb-1">
            No users yet
          </h3>
          <p className="text-sm text-[#9AA6B2]">
            Invite team members to your workspace
          </p>
        </div>
      ) : (
        <div className="bg-[#1f2937] border border-[#374151] rounded-2xl overflow-hidden app-panel-soft">
          <table className="w-full">
            <thead>
              <tr className="border-b border-[#374151]">
                <th className="text-left text-xs font-semibold text-[#9AA6B2] uppercase tracking-wider px-5 py-3">
                  User
                </th>
                <th className="text-left text-xs font-semibold text-[#9AA6B2] uppercase tracking-wider px-5 py-3">
                  Role
                </th>
                <th className="text-left text-xs font-semibold text-[#9AA6B2] uppercase tracking-wider px-5 py-3">
                  Company
                </th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-[#374151]">
              {users.map((user) => (
                <tr
                  key={user.id}
                  className="hover:bg-[#374151]/30 transition-colors"
                >
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="
                        w-8 h-8 rounded-full bg-[#3f51b5]
                        flex items-center justify-center
                        text-sm font-semibold text-white flex-shrink-0
                      ">
                        {user.email[0].toUpperCase()}
                      </div>
                      <div>
                        <p className="text-sm text-[#E6EEF2] font-medium">
                          {user.email}
                        </p>
                        {user.email === currentEmail && (
                          <p className="text-xs text-[#3f51b5]">You</p>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4">
                    <RoleBadge role={user.role} />
                  </td>
                  <td className="px-5 py-4">
                    <span className="text-sm text-[#9AA6B2]">{user.companyName}</span>
                  </td>
                  <td className="px-5 py-4 text-right">
                    <button
                      onClick={() => handleDelete(user)}
                      disabled={deletingId === user.id || user.email === currentEmail}
                      className="
                        p-1.5 rounded-lg text-[#9AA6B2]
                        hover:text-[#E53935] hover:bg-[#E53935]/10
                        disabled:opacity-30 disabled:cursor-not-allowed
                        transition-all
                      "
                      title={user.email === currentEmail ? "Can't delete yourself" : 'Delete user'}
                    >
                      {deletingId === user.id
                        ? <span className="w-4 h-4 border-2 border-[#374151] border-t-[#E53935] rounded-full animate-spin block" />
                        : <Trash2 className="w-4 h-4" />
                      }
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showAdd && (
        <AddUserModal
          onClose={() => setShowAdd(false)}
          onCreated={handleCreated}
        />
      )}
    </div>
  )
}