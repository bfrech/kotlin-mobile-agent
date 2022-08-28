package com.example.kotlin_agent.activities.messages

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_agent.Utils


class MessageListAdapter(userMessageList: List<UserMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    private var mUserMessageList: List<UserMessage> = userMessageList.sortedBy { it.createdAt }


    override fun getItemViewType(position: Int): Int {
        val userMessage: UserMessage = mUserMessageList[position]
        return if (userMessage.sender == "sent") {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            val view =
                LayoutInflater.from(parent.context).inflate(com.example.kotlin_agent.R.layout.message_item_self, parent, false)
            SentMessageHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(com.example.kotlin_agent.R.layout.message_item_they, parent, false)
            ReceivedMessageHolder(view)
        }

    }


    override fun getItemCount(): Int {
        return mUserMessageList.size
    }

    fun updateMessageList(newList: List<UserMessage>) {
        mUserMessageList = newList.sortedBy { it.createdAt }
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mUserMessageList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message)
        }
    }



    // View Holder for Received messages
    class ReceivedMessageHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private var messageText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_message_other) as TextView
        private var timeText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_timestamp_other) as TextView
        private var nameText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_user_other) as TextView
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(userMessage: UserMessage) {
            messageText.text = userMessage.message
            timeText.text = Utils.formatDateTime(userMessage.createdAt)
            nameText.text = userMessage.sender
        }

    }

    // View Holder for Sent messages
    class SentMessageHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private var messageText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_message_me) as TextView
        private var timeText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_timestamp_me) as TextView

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(userMessage: UserMessage) {
            messageText.text = userMessage.message
            timeText.text = Utils.formatDateTime(userMessage.createdAt)
        }
    }




}