package com.example.kotlin_agent.activities.messages

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_agent.Utils


class MessageListAdapter(context: Context, userMessageList: List<UserMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    private val mContext: Context = context
    private var mUserMessageList: List<UserMessage> = userMessageList


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

    fun updateMessageList(newlist: List<UserMessage>) {
        mUserMessageList = newlist
        notifyDataSetChanged()
    }

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
        var messageText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_message_other) as TextView
        var timeText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_timestamp_other) as TextView
        var nameText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_user_other) as TextView
        fun bind(userMessage: UserMessage) {
            messageText.text = userMessage.message

            // Format the stored timestamp into a readable String using method.
            timeText.text = Utils.formatDateTime(userMessage.createdAt)
            nameText.text = userMessage.sender
        }

    }


    class SentMessageHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_message_me) as TextView
        var timeText: TextView = itemView.findViewById<View>(com.example.kotlin_agent.R.id.text_timestamp_me) as TextView

        fun bind(userMessage: UserMessage) {
            messageText.text = userMessage.message

            // Format the stored timestamp into a readable String using method.
            timeText.text = Utils.formatDateTime(userMessage.createdAt)
        }
    }




}