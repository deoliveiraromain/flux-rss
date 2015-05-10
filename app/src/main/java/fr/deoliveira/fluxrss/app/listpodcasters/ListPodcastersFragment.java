package fr.deoliveira.fluxrss.app.listpodcasters;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import fr.deoliveira.fluxrss.app.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListPodcastersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListPodcastersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListPodcastersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final int internalRequestCode=568057870;
    private ArrayAdapter<ListPodcasters> podcastInfoArrayAdapter;
    private ListView listViewPodcaters;


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListPodcastersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListPodcastersFragment newInstance(String param1, String param2) {
        ListPodcastersFragment fragment = new ListPodcastersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ListPodcastersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_list_podcasteur, container, false);
        listViewPodcaters = (ListView) rootView.findViewById(android.R.id.list);
        loadPodcast();
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        //TODO : ici qu'on g�re un item cliqu� dans la liste des podcasters., on envoie les infos n�cessaire au changement de fragment
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    private int loadPodcast() {
        // TODO : ici le provider ira chercher les podcasteurs EN BDD, pour chaque on a une ligne dans l'adapter
        ListPodcastersProviderInterface podcastersProviderInterface = new ListPodcastersProvider();
        List<ListPodcasters> podcasts = podcastersProviderInterface.getListPodcasters();
        Bind(podcasts);
        return podcasts.size();
    }

    private void Bind(List<ListPodcasters> podcasts) {
        this.podcastInfoArrayAdapter = new ListPodcastersAdapter(this.getActivity().getBaseContext(), podcasts);
        this.listViewPodcaters.setAdapter(podcastInfoArrayAdapter);


    }

}